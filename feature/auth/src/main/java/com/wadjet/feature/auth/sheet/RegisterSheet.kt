package com.wadjet.feature.auth.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.component.WadjetButton
import com.wadjet.core.designsystem.component.WadjetGhostButton
import com.wadjet.core.designsystem.component.WadjetTextField
import com.wadjet.feature.auth.AuthUiState
import com.wadjet.feature.auth.AuthViewModel
import com.wadjet.feature.auth.R
import com.wadjet.core.designsystem.R as DesignR
import androidx.compose.ui.res.stringResource

@Composable
fun RegisterSheet(
    state: AuthUiState,
    onRegister: (email: String, password: String, confirmPassword: String, displayName: String?) -> Unit,
    onGoogleSignIn: () -> Unit,
    onSwitchToLogin: () -> Unit,
) {
    var displayName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val strength = AuthViewModel.passwordStrength(password)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineMedium,
            color = WadjetColors.Gold,
        )

        WadjetTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = stringResource(R.string.register_display_name),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        WadjetTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(R.string.register_email_label),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        WadjetTextField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(R.string.register_password_label),
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (passwordVisible) stringResource(DesignR.string.action_hide_password) else stringResource(DesignR.string.action_show_password),
                        tint = WadjetColors.TextMuted,
                    )
                }
            },
        )

        // Password strength bar
        if (password.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { strength },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = when {
                        strength < 0.4f -> MaterialTheme.colorScheme.error
                        strength < 0.7f -> WadjetColors.Sand
                        else -> WadjetColors.Success
                    },
                    trackColor = WadjetColors.Border,
                )

                // Password rules
                PasswordRule(stringResource(R.string.register_rule_length), password.length >= 8)
                PasswordRule(stringResource(R.string.register_rule_uppercase), password.any { it.isUpperCase() })
                PasswordRule(stringResource(R.string.register_rule_lowercase), password.any { it.isLowerCase() })
                PasswordRule(stringResource(R.string.register_rule_digit), password.any { it.isDigit() })
            }
        }

        WadjetTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = stringResource(R.string.register_confirm_password),
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (confirmPasswordVisible) stringResource(DesignR.string.action_hide_password) else stringResource(DesignR.string.action_show_password),
                        tint = WadjetColors.TextMuted,
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onRegister(email, password, confirmPassword, displayName.ifBlank { null }) }),
        )

        if (confirmPassword.isNotEmpty() && password != confirmPassword) {
            Text(
                text = stringResource(R.string.register_password_mismatch),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        WadjetButton(
            text = if (state.isLoading) stringResource(R.string.register_creating_account) else stringResource(R.string.register_title),
            onClick = { onRegister(email, password, confirmPassword, displayName.ifBlank { null }) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = WadjetColors.Border)
            Text(stringResource(R.string.login_divider_or), color = WadjetColors.TextMuted, style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(modifier = Modifier.weight(1f), color = WadjetColors.Border)
        }

        WadjetGhostButton(
            text = stringResource(R.string.register_sign_up_google),
            onClick = onGoogleSignIn,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.register_have_account), color = WadjetColors.TextMuted, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onSwitchToLogin) {
                Text(stringResource(R.string.register_sign_in), color = WadjetColors.Gold)
            }
        }
    }
}

@Composable
private fun PasswordRule(text: String, met: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = if (met) "✓" else "✗",
            color = if (met) WadjetColors.Success else WadjetColors.TextMuted,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = text,
            color = if (met) WadjetColors.Text else WadjetColors.TextMuted,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
