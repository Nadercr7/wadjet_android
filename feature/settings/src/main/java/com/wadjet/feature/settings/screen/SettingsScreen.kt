package com.wadjet.feature.settings.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.feature.settings.SettingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onStartEditName: () -> Unit,
    onUpdateEditName: (String) -> Unit,
    onSaveName: () -> Unit,
    onCancelEditName: () -> Unit,
    onUpdateCurrentPassword: (String) -> Unit,
    onUpdateNewPassword: (String) -> Unit,
    onChangePassword: () -> Unit,
    onTtsEnabledChanged: (Boolean) -> Unit,
    onTtsSpeedChanged: (Float) -> Unit,
    onClearCache: () -> Unit,
    onSignOut: () -> Unit,
    onFeedback: () -> Unit,
    onDismissMessage: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showSignOutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            onDismissMessage()
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out", color = WadjetColors.Text) },
            text = { Text("Are you sure you want to sign out?", color = WadjetColors.TextMuted) },
            confirmButton = {
                TextButton(onClick = { showSignOutDialog = false; onSignOut() }) {
                    Text("Sign Out", color = WadjetColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = WadjetColors.TextMuted)
                }
            },
            containerColor = WadjetColors.Surface,
        )
    }

    Scaffold(
        containerColor = WadjetColors.Night,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", color = WadjetColors.Gold, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = WadjetColors.Text)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WadjetColors.Surface),
            )
        },
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Profile Section ──
            item { SectionHeader("Profile") }
            item {
                ProfileSection(
                    state = state,
                    onStartEdit = onStartEditName,
                    onUpdateName = onUpdateEditName,
                    onSave = onSaveName,
                    onCancel = onCancelEditName,
                )
            }

            // ── Password (email auth only) ──
            if (state.user?.authProvider == "email" || state.user?.authProvider == "password") {
                item { SectionHeader("Password") }
                item {
                    PasswordSection(
                        currentPassword = state.currentPassword,
                        newPassword = state.newPassword,
                        isChanging = state.isChangingPassword,
                        onUpdateCurrent = onUpdateCurrentPassword,
                        onUpdateNew = onUpdateNewPassword,
                        onChange = onChangePassword,
                    )
                }
            }

            // ── TTS Settings ──
            item { SectionHeader("Text-to-Speech") }
            item {
                TtsSection(
                    enabled = state.ttsEnabled,
                    speed = state.ttsSpeed,
                    onEnabledChanged = onTtsEnabledChanged,
                    onSpeedChanged = onTtsSpeedChanged,
                )
            }

            // ── Storage ──
            item { SectionHeader("Storage") }
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Cache", color = WadjetColors.Text, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${state.cacheSizeMb} MB",
                                color = WadjetColors.TextMuted,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(WadjetColors.Surface)
                                .border(1.dp, WadjetColors.Gold, RoundedCornerShape(8.dp))
                                .clickable(onClick = onClearCache)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text("Clear Cache", color = WadjetColors.Gold, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // ── About ──
            item { SectionHeader("About") }
            item {
                SettingsCard {
                    Text("Wadjet v1.0.0-beta", color = WadjetColors.Text, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Built by Mr Robot", color = WadjetColors.TextMuted, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Send Feedback",
                        color = WadjetColors.Gold,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.clickable(onClick = onFeedback),
                    )
                }
            }

            // ── Account ──
            item { SectionHeader("Account") }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(WadjetColors.Error.copy(alpha = 0.08f))
                        .border(1.dp, WadjetColors.Error.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { showSignOutDialog = true }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Sign Out", color = WadjetColors.Error, style = MaterialTheme.typography.labelLarge)
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun ProfileSection(
    state: SettingsUiState,
    onStartEdit: () -> Unit,
    onUpdateName: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(modifier = modifier) {
        if (state.isEditingName) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = state.editName,
                    onValueChange = onUpdateName,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = settingsTextFieldColors(),
                    placeholder = { Text("Display name", color = WadjetColors.TextMuted) },
                )
                IconButton(onClick = onSave) {
                    Icon(Icons.Default.Check, "Save", tint = WadjetColors.Gold)
                }
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, "Cancel", tint = WadjetColors.TextMuted)
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.user?.displayName ?: "No name set",
                        color = WadjetColors.Text,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = state.user?.email ?: "",
                        color = WadjetColors.TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                // Provider badge
                state.user?.authProvider?.let { provider ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(WadjetColors.Gold.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = if (provider.contains("google")) "Google" else "Email",
                            color = WadjetColors.Gold,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onStartEdit) {
                    Icon(Icons.Default.Edit, "Edit name", tint = WadjetColors.Sand)
                }
            }
        }
    }
}

@Composable
private fun PasswordSection(
    currentPassword: String,
    newPassword: String,
    isChanging: Boolean,
    onUpdateCurrent: (String) -> Unit,
    onUpdateNew: (String) -> Unit,
    onChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(modifier = modifier) {
        TextField(
            value = currentPassword,
            onValueChange = onUpdateCurrent,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Current Password", color = WadjetColors.TextMuted) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = settingsTextFieldColors(),
            enabled = !isChanging,
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = newPassword,
            onValueChange = onUpdateNew,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("New Password", color = WadjetColors.TextMuted) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = settingsTextFieldColors(),
            enabled = !isChanging,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(WadjetColors.Gold)
                .clickable(enabled = !isChanging, onClick = onChange)
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            Text(
                text = if (isChanging) "Saving..." else "Change Password",
                color = WadjetColors.Night,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun TtsSection(
    enabled: Boolean,
    speed: Float,
    onEnabledChanged: (Boolean) -> Unit,
    onSpeedChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Enable TTS", color = WadjetColors.Text, style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = WadjetColors.Gold,
                    checkedTrackColor = WadjetColors.Gold.copy(alpha = 0.3f),
                    uncheckedThumbColor = WadjetColors.TextMuted,
                    uncheckedTrackColor = WadjetColors.Surface,
                ),
            )
        }
        if (enabled) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Speed", color = WadjetColors.TextMuted, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = speed,
                    onValueChange = onSpeedChanged,
                    valueRange = 0.5f..2.0f,
                    steps = 5,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = WadjetColors.Gold,
                        activeTrackColor = WadjetColors.Gold,
                        inactiveTrackColor = WadjetColors.Border,
                    ),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${String.format("%.1f", speed)}x",
                    color = WadjetColors.Sand,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = WadjetColors.Gold,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WadjetColors.SurfaceAlt)
            .border(1.dp, WadjetColors.Border, RoundedCornerShape(12.dp))
            .padding(16.dp),
        content = content,
    )
}

@Composable
private fun settingsTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = WadjetColors.Surface,
    unfocusedContainerColor = WadjetColors.Surface,
    focusedTextColor = WadjetColors.Text,
    unfocusedTextColor = WadjetColors.Text,
    cursorColor = WadjetColors.Gold,
    focusedIndicatorColor = WadjetColors.Gold,
    unfocusedIndicatorColor = WadjetColors.Border,
)
