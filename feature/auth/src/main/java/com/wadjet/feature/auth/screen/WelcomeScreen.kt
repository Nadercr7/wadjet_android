package com.wadjet.feature.auth.screen

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.WadjetTypography
import com.wadjet.core.designsystem.HieroglyphStyle
import com.wadjet.core.designsystem.component.WadjetButton
import com.wadjet.core.designsystem.component.WadjetCard
import com.wadjet.core.designsystem.component.WadjetGhostButton
import com.wadjet.core.designsystem.R as DesignR
import com.wadjet.feature.auth.AuthEvent
import com.wadjet.feature.auth.AuthSheet
import com.wadjet.feature.auth.AuthViewModel
import com.wadjet.feature.auth.sheet.ForgotPasswordSheet
import com.wadjet.feature.auth.sheet.LoginSheet
import com.wadjet.feature.auth.sheet.RegisterSheet
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    webClientId: String,
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activeSheet by viewModel.activeSheet.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.AuthSuccess -> onAuthSuccess()
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // Logo & tagline
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(DesignR.drawable.logo_wadjet),
                contentDescription = "Wadjet logo",
                modifier = Modifier.size(120.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "WADJET",
                style = MaterialTheme.typography.headlineLarge,
                color = WadjetColors.Gold,
                letterSpacing = MaterialTheme.typography.headlineLarge.letterSpacing,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Decode the Secrets\nof Ancient Egypt",
                style = MaterialTheme.typography.titleLarge,
                color = WadjetColors.Gold,
                textAlign = TextAlign.Center,
            )
        }

        // Feature preview cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            item { FeatureCard("𓂀", "Scan", "Decode hieroglyphs") }
            item { FeatureCard("𓊹", "Dictionary", "1,000+ signs") }
            item { FeatureCard("🏛", "Explore", "260+ landmarks") }
        }

        // Auth buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Google Sign-In
            WadjetGhostButton(
                text = "Sign in with Google",
                onClick = {
                    scope.launch {
                        try {
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setServerClientId(webClientId)
                                .setFilterByAuthorizedAccounts(false)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val result = credentialManager.getCredential(
                                context as Activity,
                                request,
                            )
                            val googleIdToken = GoogleIdTokenCredential
                                .createFrom(result.credential.data)
                                .idToken
                            viewModel.signInWithGoogle(googleIdToken)
                        } catch (e: Exception) {
                            Timber.e(e, "Google sign-in failed")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            // Email sign-up
            WadjetButton(
                text = "Sign up with Email",
                onClick = { viewModel.showSheet(AuthSheet.REGISTER) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
            )

            // Already have account
            androidx.compose.material3.TextButton(
                onClick = { viewModel.showSheet(AuthSheet.LOGIN) },
            ) {
                Text(
                    text = "Already have an account? Sign in",
                    color = WadjetColors.Sand,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        // Footer
        Text(
            text = "Built by Mr Robot",
            style = MaterialTheme.typography.bodySmall,
            color = WadjetColors.Dust,
        )
    }

    // Bottom sheets
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (activeSheet != AuthSheet.NONE) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissSheet() },
            sheetState = sheetState,
            containerColor = WadjetColors.Surface,
            contentColor = WadjetColors.Text,
        ) {
            when (activeSheet) {
                AuthSheet.LOGIN -> LoginSheet(
                    state = state,
                    onSignIn = { email, password -> viewModel.signInWithEmail(email, password) },
                    onGoogleSignIn = {
                        scope.launch {
                            try {
                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setServerClientId(webClientId)
                                    .setFilterByAuthorizedAccounts(false)
                                    .build()
                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()
                                val result = credentialManager.getCredential(
                                    context as Activity,
                                    request,
                                )
                                val idToken = GoogleIdTokenCredential
                                    .createFrom(result.credential.data)
                                    .idToken
                                viewModel.signInWithGoogle(idToken)
                            } catch (e: Exception) {
                                Timber.e(e, "Google sign-in failed")
                            }
                        }
                    },
                    onForgotPassword = { viewModel.showSheet(AuthSheet.FORGOT_PASSWORD) },
                    onSwitchToRegister = { viewModel.showSheet(AuthSheet.REGISTER) },
                )
                AuthSheet.REGISTER -> RegisterSheet(
                    state = state,
                    onRegister = { email, password, confirm, name ->
                        viewModel.register(email, password, confirm, name)
                    },
                    onGoogleSignIn = {
                        scope.launch {
                            try {
                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setServerClientId(webClientId)
                                    .setFilterByAuthorizedAccounts(false)
                                    .build()
                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()
                                val result = credentialManager.getCredential(
                                    context as Activity,
                                    request,
                                )
                                val idToken = GoogleIdTokenCredential
                                    .createFrom(result.credential.data)
                                    .idToken
                                viewModel.signInWithGoogle(idToken)
                            } catch (e: Exception) {
                                Timber.e(e, "Google sign-in failed")
                            }
                        }
                    },
                    onSwitchToLogin = { viewModel.showSheet(AuthSheet.LOGIN) },
                )
                AuthSheet.FORGOT_PASSWORD -> ForgotPasswordSheet(
                    state = state,
                    onSendReset = { email -> viewModel.forgotPassword(email) },
                    onBackToLogin = { viewModel.showSheet(AuthSheet.LOGIN) },
                )
                AuthSheet.NONE -> {}
            }
        }
    }
}

@Composable
private fun FeatureCard(glyph: String, title: String, subtitle: String) {
    WadjetCard(
        modifier = Modifier.size(width = 140.dp, height = 120.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = glyph, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = WadjetColors.Gold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = WadjetColors.TextMuted)
        }
    }
}
