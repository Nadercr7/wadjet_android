package com.wadjet.feature.feedback.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.component.WadjetButton
import com.wadjet.core.designsystem.R as DesignR
import com.wadjet.feature.feedback.FEEDBACK_CATEGORIES
import com.wadjet.feature.feedback.FeedbackUiState
import com.wadjet.feature.feedback.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedbackScreen(
    state: FeedbackUiState,
    onCategorySelected: (String) -> Unit,
    onMessageChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = WadjetColors.Night,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.feedback_title), color = WadjetColors.Gold, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(DesignR.string.action_back), tint = WadjetColors.Text)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WadjetColors.Surface),
            )
        },
        modifier = modifier,
    ) { padding ->
        if (state.isSuccess) {
            SuccessContent(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Category chips
            item {
                Text(stringResource(R.string.feedback_category_label), color = WadjetColors.Text, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FEEDBACK_CATEGORIES.forEach { category ->
                        FilterChip(
                            selected = state.selectedCategory == category,
                            onClick = { onCategorySelected(category) },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WadjetColors.Gold,
                                selectedLabelColor = WadjetColors.Night,
                                containerColor = WadjetColors.Surface,
                                labelColor = WadjetColors.TextMuted,
                            ),
                        )
                    }
                }
            }

            // Message
            item {
                Text(stringResource(R.string.feedback_message_label), color = WadjetColors.Text, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = state.message,
                    onValueChange = onMessageChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    placeholder = { Text(stringResource(R.string.feedback_message_placeholder), color = WadjetColors.TextMuted) },
                    colors = feedbackTextFieldColors(),
                    maxLines = 8,
                )
                Text(
                    text = stringResource(R.string.feedback_char_count, state.message.length),
                    color = if (state.message.length >= 1000) WadjetColors.Error else WadjetColors.TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            }

            // Optional fields
            item {
                Text(
                    stringResource(R.string.feedback_optional_label),
                    color = WadjetColors.TextMuted,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = state.name,
                    onValueChange = onNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.feedback_name_placeholder), color = WadjetColors.TextMuted) },
                    colors = feedbackTextFieldColors(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = state.email,
                    onValueChange = onEmailChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.feedback_email_placeholder), color = WadjetColors.TextMuted) },
                    colors = feedbackTextFieldColors(),
                    singleLine = true,
                )
            }

            // Error
            if (state.error != null) {
                item {
                    Text(
                        text = state.error,
                        color = WadjetColors.Error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            // Submit
            item {
                WadjetButton(
                    text = stringResource(R.string.feedback_button),
                    onClick = onSubmit,
                    isLoading = state.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SuccessContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✓", fontSize = 64.sp, color = WadjetColors.Success)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.feedback_success_title),
                    color = WadjetColors.Gold,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.feedback_success_message),
                    color = WadjetColors.TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun feedbackTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = WadjetColors.Surface,
    unfocusedContainerColor = WadjetColors.Surface,
    focusedTextColor = WadjetColors.Text,
    unfocusedTextColor = WadjetColors.Text,
    cursorColor = WadjetColors.Gold,
    focusedIndicatorColor = WadjetColors.Gold,
    unfocusedIndicatorColor = WadjetColors.Border,
)
