package com.tseyler.livetemplates.sharing

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.runBlocking

private val logger = logger<ProjectLiveTemplatesStartupActivity>()

class ProjectLiveTemplatesStartupActivity: ProjectActivity, ProjectManagerListener {
    override suspend fun execute(project: Project) {
        logger.info("Trying to sync project live templates...")
        syncTemplatesFromProject(project)
    }

    override fun projectClosingBeforeSave(project: Project) {
        runBlocking(LiveTemplatesSharingService.scope.coroutineContext) {
            logger.info("Removing synced templates from project if any...")
            removeSyncedTemplates(project)
        }
    }
}

