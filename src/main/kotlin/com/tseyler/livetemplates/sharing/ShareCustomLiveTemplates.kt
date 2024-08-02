package com.tseyler.livetemplates.sharing

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import kotlinx.coroutines.launch

class ShareCustomLiveTemplates: AnAction() {
    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        LiveTemplatesSharingService.scope.launch {
            saveTemplates()
            if (!syncTemplatesToProject(project)) {
                runInEdt {
                    Messages.showInfoMessage("No custom live templates found in IDE Settings", "Share Custom Live Templates")
                }
                return@launch
            }
            val path = project.templatesProjectPath
            val file = VfsUtil.findFile(path, true)
            val projectView = ProjectView.getInstance(project)
            projectView.select(file, file, true)
            projectView.refresh()
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}