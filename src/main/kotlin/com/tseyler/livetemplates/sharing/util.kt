@file:OptIn(ExperimentalEncodingApi::class)

package com.tseyler.livetemplates.sharing

import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.configurationStore.schemeManager.SchemeManagerFactoryBase
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.options.SchemeManagerFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.nameWithoutExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val ourLock = Any()
private val String.withoutXml: String
    get() = substringBeforeLast(".xml")

private val SHARED_TEMPLATES = Key.create<Set<SyncedFile>>("SHARED_TEMPLATES")

private val Path.ifExists: Path?
    get() {
        if (!Files.exists(this)) {
            return null
        }
        return this
    }

val Project.templatesProjectPath: Path
    get() {
        return Paths.get("$basePath/.idea/liveTemplates")
    }

val Project.templatesProjectPathIfExists: Path?
    get() = templatesProjectPath.ifExists

val templatesSettingsPath: Path by lazy {
    Paths.get(PathManager.getConfigPath()).resolve("templates")
}

val templatesSettingsPathIfExists: Path?
    get() = templatesSettingsPath.ifExists

suspend fun syncTemplatesFromProject(project: Project) {
    val from = project.templatesProjectPathIfExists ?: return
    val sharedFiles = syncTemplates(from, ensurePathExists(templatesSettingsPath)) { path ->
        !isDefaultTemplateGroup(path)
    }
    if (sharedFiles.isNotEmpty()) {
        syncToIde()
        addSharedFiles(project, sharedFiles)
    }
}

suspend fun syncTemplatesToProject(project: Project) {
    val from = templatesSettingsPathIfExists ?: return
    syncTemplates(from, ensurePathExists(project.templatesProjectPath)) { path ->
        !isDefaultTemplateGroup(path)
    }
}

suspend fun removeSyncedTemplates(project: Project) {
    val syncedTemplateGroups = project.getUserData(SHARED_TEMPLATES) ?: return
    val templatesPath = templatesSettingsPathIfExists ?: return

    for (group in syncedTemplateGroups) {
        withContext(Dispatchers.IO) {
            val fileName = group.fileName
            val path = templatesPath.resolve(fileName)
            val synced = path.asSynced()
            if (synced == group) {
                removeGroupFromSettings(fileName)
                Files.deleteIfExists(path)
            }
        }
    }
    project.putUserData(SHARED_TEMPLATES, emptySet())
}

private fun isDefaultTemplateGroup(path: Path): Boolean {
    val settings = TemplateSettings.getInstance()
    val group = settings.templateGroups.find { it.name == path.fileName.nameWithoutExtension } ?: return false
    return settings.templates.mapNotNull { settings.getDefaultTemplate(it) }.any { it.groupName == group.name }
}

private fun removeGroupFromSettings(fileName: String) {
    val groupFromSettings = TemplateSettings.getInstance().templateGroups.find { it.name == fileName.withoutXml }
    groupFromSettings?.elements?.forEach {
        TemplateSettings.getInstance().removeTemplate(it)
    }
}

private suspend fun syncTemplates(from: Path, to: Path, filter: (Path) -> Boolean): Set<SyncedFile> {
    val allFiles = withContext(Dispatchers.IO) {
        Files.list(from).use { it.toList() }
    }
    val sharedFiles = mutableSetOf<SyncedFile>()
    for (fromTemplateGroup in allFiles) {
        val groupToCreate = to.resolve(fromTemplateGroup.fileName)
        if (!filter(groupToCreate) || Files.exists(groupToCreate)) {
            continue
        }

        if (fromTemplateGroup.fileName.toString().endsWith(".xml")) {
            withContext(Dispatchers.IO) {
                Files.copy(fromTemplateGroup, groupToCreate)
            }
            sharedFiles += SyncedFile(
                hash(fromTemplateGroup),
                fromTemplateGroup.fileName.toString()
            )
        }
    }
    return sharedFiles
}

private suspend fun ensurePathExists(path: Path): Path {
    if (!Files.exists(path)) {
        withContext(Dispatchers.IO) {
            Files.createDirectory(path)
        }
    }
    return path
}

private fun addSharedFiles(project: Project, files: Set<SyncedFile>) {
    synchronized(ourLock) {
        val sharedTemplates = project.getUserData(SHARED_TEMPLATES) ?: emptySet()
        project.putUserData(SHARED_TEMPLATES, sharedTemplates + files)
    }
}

private fun Path.asSynced(): SyncedFile {
    return SyncedFile(hash(this), fileName.toString())
}

private fun hash(path: Path): String {
    val bytes = Files.readAllBytes(path)
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return Base64.encode(digest)
}

private fun syncToIde() {
    val instance = SchemeManagerFactory.getInstance()
    instance as SchemeManagerFactoryBase
    instance.process { f ->
        if (f.fileSpec == "templates") {
            f.reload()
        }
    }
}

data class SyncedFile(val hash: String, val fileName: String)
