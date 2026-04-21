package com.wadjet.feature.explore.image

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Thin ViewModel that exposes [ThumbnailResolver] to composables via Hilt and
 * runs the Pexels lookup in [viewModelScope] so it survives configuration
 * changes.
 */
@HiltViewModel
class ThumbnailFallbackViewModel @Inject constructor(
    val resolver: ThumbnailResolver,
) : ViewModel() {
    fun resolveAsync(slug: String, name: String, onResolved: (String?) -> Unit) {
        viewModelScope.launch {
            onResolved(resolver.resolve(slug, name))
        }
    }
}
