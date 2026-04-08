package com.wadjet.core.common

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
