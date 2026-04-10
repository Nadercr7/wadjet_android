package com.wadjet.feature.auth.sheet

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.component.WadjetButton
import com.wadjet.core.designsystem.component.WadjetGhostButton
import com.wadjet.core.designsystem.component.WadjetTextField
import com.wadjet.feature.auth.AuthUiState

@Composable
fun ForgotPasswordSheet(
    state: AuthUiState,
    onSendReset: (email: String) -> Unit,
    onBackToLogin: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Reset Password",
            style = MaterialTheme.typography.headlineMedium,
            color = WadjetColors.Gold,
        )

        if (state.forgotPasswordSent) {
            // Success state
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "𓂋",
                    style = MaterialTheme.typography.displaySmall,
                    fontFamily = com.wadjet.core.designsystem.NotoSansEgyptianHieroglyphs,
                    color = WadjetColors.Gold,
                )
                Text(
                    text = "Check your inbox",
                    style = MaterialTheme.typography.titleMedium,
                    color = WadjetColors.Text,
                )
                Text(
                    text = "We sent a password reset link to your email.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WadjetColors.TextMuted,
                )

                Spacer(modifier = Modifier.height(8.dp))

                WadjetGhostButton(
                    text = "Open Email App",
                    onClick = {
                        val intent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_EMAIL)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            // Input state
            Text(
                text = "Enter your email and we'll send a reset link.",
                style = MaterialTheme.typography.bodyMedium,
                color = WadjetColors.TextMuted,
            )

            WadjetTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            WadjetButton(
                text = if (state.isLoading) "Sending…" else "Send Reset Link",
                onClick = { onSendReset(email) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && email.isNotBlank(),
            )
        }

        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.align(Alignment.Start),
        ) {
            Text("← Back to Sign In", color = WadjetColors.Sand)
        }
    }
}
