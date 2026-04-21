package com.wadjet.feature.auth.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.component.WadjetButton
import com.wadjet.core.designsystem.component.WadjetGhostButton
import com.wadjet.feature.auth.AuthUiState
import com.wadjet.feature.auth.R

@Composable
fun VerifyEmailSheet(
    state: AuthUiState,
    onCheckVerified: () -> Unit,
    onResend: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = CircleShape,
            color = WadjetColors.Gold.copy(alpha = 0.12f),
            modifier = Modifier.size(72.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.MarkEmailUnread,
                    contentDescription = null,
                    tint = WadjetColors.Gold,
                    modifier = Modifier.size(36.dp),
                )
            }
        }

        Text(
            text = stringResource(R.string.verify_title),
            style = MaterialTheme.typography.headlineSmall,
            color = WadjetColors.Gold,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )

        val email = state.pendingVerificationEmail
        Text(
            text = if (email != null) {
                stringResource(R.string.verify_instructions_with_email, email)
            } else {
                stringResource(R.string.verify_instructions)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = WadjetColors.TextMuted,
            textAlign = TextAlign.Center,
        )

        if (state.verificationSent) {
            Text(
                text = stringResource(R.string.verify_sent_confirmation),
                style = MaterialTheme.typography.bodySmall,
                color = WadjetColors.Success,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (state.verificationCheckFailed) {
            Text(
                text = stringResource(R.string.verify_not_yet),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        WadjetButton(
            text = if (state.isLoading) stringResource(R.string.verify_checking) else stringResource(R.string.verify_i_verified),
            onClick = onCheckVerified,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
        )

        WadjetGhostButton(
            text = stringResource(R.string.verify_resend),
            onClick = onResend,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
        )

        TextButton(onClick = onCancel, enabled = !state.isLoading) {
            Text(
                text = stringResource(R.string.verify_cancel),
                color = WadjetColors.TextMuted,
            )
        }
    }
}
