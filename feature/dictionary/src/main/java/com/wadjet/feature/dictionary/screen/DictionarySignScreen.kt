package com.wadjet.feature.dictionary.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wadjet.core.designsystem.WadjetColors
import androidx.compose.ui.res.stringResource
import com.wadjet.core.designsystem.R as DesignR
import com.wadjet.feature.dictionary.R
import com.wadjet.feature.dictionary.SignDetailViewModel
import com.wadjet.feature.dictionary.sheet.SignDetailSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionarySignScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WadjetColors.Night),
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.sign_detail_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(DesignR.string.action_back))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = WadjetColors.Night,
                titleContentColor = WadjetColors.Sand,
                navigationIconContentColor = WadjetColors.Sand,
            ),
        )

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = WadjetColors.Gold)
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = state.error ?: stringResource(DesignR.string.error_unknown),
                        color = WadjetColors.Error,
                    )
                }
            }
            state.sign != null -> {
                SignDetailSheet(
                    sign = state.sign!!,
                    isFavorite = state.isFavorite,
                    onSpeak = viewModel::speakSign,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onShowToast = { viewModel.showToast(it) },
                    modifier = Modifier.padding(),
                )
            }
        }
    }
}
