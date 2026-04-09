package com.wadjet.core.common

import kotlin.coroutines.cancellation.CancellationException

/**
 * NiA-style suspendRunCatching that re-throws CancellationException.
 *
 * Standard [runCatching] catches CancellationException, which breaks
 * structured concurrency. This version preserves cancellation semantics.
 */
suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
