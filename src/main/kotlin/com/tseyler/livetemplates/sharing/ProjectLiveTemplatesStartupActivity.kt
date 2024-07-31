package com.tseyler.livetemplates.sharing

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.runBlocking

class ProjectLiveTemplatesStartupActivity: ProjectActivity, ProjectManagerListener {
    override suspend fun execute(project: Project) {
        syncTemplatesFromProject(project)
    }

    override fun projectClosingBeforeSave(project: Project) {
        runBlocking(LiveTemplatesSharingService.scope.coroutineContext) {
            removeSyncedTemplates(project)
        }
    }
}

