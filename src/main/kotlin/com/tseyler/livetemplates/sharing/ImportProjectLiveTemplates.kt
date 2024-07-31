package com.tseyler.livetemplates.sharing

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import kotlinx.coroutines.launch

class ImportProjectLiveTemplates: AnAction() {
    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null && project.templatesProjectPathIfExists != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        LiveTemplatesSharingService.scope.launch {
            syncTemplatesFromProject(project)
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}