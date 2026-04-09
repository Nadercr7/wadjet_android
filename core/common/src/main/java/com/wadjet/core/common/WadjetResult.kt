package com.wadjet.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

sealed class WadjetResult<out T> {
    data class Success<T>(val data: T) : WadjetResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : WadjetResult<Nothing>()
    data object Loading : WadjetResult<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error
    val isLoading get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data
    fun errorOrNull(): String? = (this as? Error)?.message
}

/**
 * NiA-style extension: converts any Flow<T> into Flow<WadjetResult<T>>
 * with automatic Loading → Success/Error transitions.
 */
fun <T> Flow<T>.asResult(): Flow<WadjetResult<T>> =
    map<T, WadjetResult<T>> { WadjetResult.Success(it) }
        .onStart { emit(WadjetResult.Loading) }
        .catch { emit(WadjetResult.Error(it.message ?: "Unknown error", it)) }
