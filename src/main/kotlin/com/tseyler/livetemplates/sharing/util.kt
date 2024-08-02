package com.tseyler.livetemplates.sharing

import com.intellij.codeInsight.template.impl.TemplateGroup
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.codeInsight.template.postfix.templates.EmptyPostfixTemplateProvider
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplatesUtils
import com.intellij.configurationStore.schemeManager.SchemeManagerFactoryBase
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.options.SchemeManagerFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.util.Key
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.nameWithoutExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val logger = logger<LiveTemplatesSharingService>()

private val ourLock = Any()

private val SYNCED_GROUPS = Key.create<Set<TemplateGroup>>("SYNCED_GROUPS")

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
    val from = project.templatesProjectPathIfExists
    if (from == null) {
        logger.info("No custom live templates found in project")
        return
    }

    val syncedGroups = mutableSetOf<TemplateGroup>()
    processTemplateGroupFiles(from) { group ->
        val instance = TemplateSettings.getInstance()
        val templateGroup = JDOMUtil.load(group)
        val groupName = templateGroup.getAttributeValue("group") ?: return@processTemplateGroupFiles false
        if (instance.templateGroups.any { it.name == groupName }) {
            logger.info("Group already exists")
            return@processTemplateGroupFiles true
        }
        var currentTemplate = PostfixTemplatesUtils.readExternalLiveTemplate(templateGroup, EmptyPostfixTemplateProvider())
        while (currentTemplate != null) {
            currentTemplate.groupName = groupName
            instance.addTemplate(currentTemplate)
            templateGroup.removeChild("template")
            currentTemplate = PostfixTemplatesUtils.readExternalLiveTemplate(templateGroup, EmptyPostfixTemplateProvider())
        }
        val syncedGroup = instance.templateGroups.firstOrNull { it.name == groupName } ?: return@processTemplateGroupFiles true
        syncedGroups += syncedGroup
        true
    }
    if (syncedGroups.isNotEmpty()) {
        logger.info("Syncing groups...")
        addSyncedGroups(project, syncedGroups)
    }
}

suspend fun syncTemplatesToProject(project: Project): Boolean {
    val from = templatesSettingsPathIfExists ?: return false
    return syncTemplateGroupsFiles(from, project.templatesProjectPath) { path ->
        !isDefaultTemplateGroup(path)
    }
}

suspend fun removeSyncedTemplates(project: Project) {
    synchronized(ourLock) {
        val syncedTemplateGroups = project.getUserData(SYNCED_GROUPS) ?: return
        for (group in syncedTemplateGroups) {
            removeGroupFromSettings(group)
        }
        project.putUserData(SYNCED_GROUPS, emptySet())
    }
    saveTemplates()
}

suspend fun saveTemplates() {
    logger.info("Saving templates...")
    val factory = SchemeManagerFactory.getInstance() as? SchemeManagerFactoryBase
    if (factory == null) {
        logger.info("Failed to cast SchemeManagerFactory")
        return
    }
    factory.save()
}

private fun isDefaultTemplateGroup(path: Path): Boolean {
    val settings = TemplateSettings.getInstance()
    val group = settings.templateGroups.find { it.name == path.fileName.nameWithoutExtension } ?: return false
    return settings.templates.mapNotNull { settings.getDefaultTemplate(it) }.any { it.groupName == group.name }
}

private fun removeGroupFromSettings(synced: TemplateGroup) {
    val groupFromSettings: TemplateGroup = TemplateSettings.getInstance().templateGroups.find { it.name == synced.name } ?: return
    val elements: List<TemplateImpl> = groupFromSettings.elements
    if (synced.elements == elements) {
        logger.info("Removing group")
        elements.forEach {
            TemplateSettings.getInstance().removeTemplate(it)
        }
    } else {
        logger.info("Group elements are changed")
    }
}

private suspend fun syncTemplateGroupsFiles(from: Path, to: Path, filter: (Path) -> Boolean): Boolean {
    return processTemplateGroupFiles(from) { fromTemplateGroup ->
        ensurePathExists(to)
        val groupToCreate = to.resolve(fromTemplateGroup.fileName)
        if (Files.exists(groupToCreate)) {
            logger.info("Group already exists")
            return@processTemplateGroupFiles true
        }
        if (!filter(groupToCreate)) {
            logger.info("Filter not passed")
            return@processTemplateGroupFiles false
        }

        Files.copy(fromTemplateGroup, groupToCreate)
        return@processTemplateGroupFiles true
    }
}

private suspend fun processTemplateGroupFiles(path: Path, processor: suspend (Path) -> Boolean): Boolean {
    val files = withContext(Dispatchers.IO) {
        Files.list(path).use { it.toList() }
    }

    var result = false
    for (file in files) {
        if (file.fileName.toString().endsWith(".xml")) {
            result = processor(file) || result
        } else {
            logger.info("Not an XML file")
        }
    }
    return result
}

private suspend fun ensurePathExists(path: Path): Path {
    withContext(Dispatchers.IO) {
        if (!Files.exists(path)) {
            Files.createDirectory(path)
        }
    }
    return path
}

private fun addSyncedGroups(project: Project, groups: Set<TemplateGroup>) {
    synchronized(ourLock) {
        val sharedTemplates = project.getUserData(SYNCED_GROUPS) ?: emptySet()
        project.putUserData(SYNCED_GROUPS, sharedTemplates + groups)
    }
}

