package com.wadjet.core.common

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class ToastType { Success, Error, Info }

data class ToastEvent(
    val message: String,
    val type: ToastType = ToastType.Info,
    val durationMs: Long = 3000L,
)

@Singleton
class ToastController @Inject constructor() {
    private val _events = Channel<ToastEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun show(message: String, type: ToastType = ToastType.Info, durationMs: Long = 3000L) {
        _events.trySend(ToastEvent(message, type, durationMs))
    }

    fun success(message: String) = show(message, ToastType.Success)
    fun error(message: String) = show(message, ToastType.Error)
    fun info(message: String) = show(message, ToastType.Info)
}
