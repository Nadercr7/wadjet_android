package com.wadjet.core.data

import retrofit2.Response

/**
 * Typed exception for API errors, carrying HTTP status code and server message.
 */
class ApiException(
    val code: Int = 0,
    override val message: String,
) : Exception(message) {
    constructor(message: String) : this(0, message)
}

/**
 * Extracts the body from a Retrofit response or throws an [ApiException].
 * Parses the error body for a "detail" field if available.
 */
fun <T> Response<T>.bodyOrThrow(): T {
    if (isSuccessful) {
        return body() ?: throw ApiException(code(), "Empty response body")
    }
    val detail = try {
        val errBody = errorBody()?.string()
        errBody?.let {
            // Try to extract "detail" from JSON error body
            val regex = """"detail"\s*:\s*"([^"]+)"""".toRegex()
            regex.find(it)?.groupValues?.get(1)
        }
    } catch (_: Exception) {
        null
    }
    throw ApiException(code(), detail ?: "Request failed: ${code()}")
}
