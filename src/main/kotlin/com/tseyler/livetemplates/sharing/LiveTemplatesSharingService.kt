package com.tseyler.livetemplates.sharing

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import kotlinx.coroutines.CoroutineScope

@Service
class LiveTemplatesSharingService(val scope: CoroutineScope) {
    companion object {
        val scope: CoroutineScope = service<LiveTemplatesSharingService>().scope
    }
}