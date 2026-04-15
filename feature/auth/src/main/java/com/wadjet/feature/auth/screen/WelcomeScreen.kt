package com.wadjet.feature.auth.screen

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.wadjet.core.designsystem.NotoSansEgyptianHieroglyphs
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.animation.FadeUp
import com.wadjet.core.designsystem.animation.GoldGradientText
import com.wadjet.core.designsystem.animation.MeteorShower
import com.wadjet.core.designsystem.component.WadjetButton
import com.wadjet.core.designsystem.component.WadjetCard
import com.wadjet.core.designsystem.component.WadjetGhostButton
import com.wadjet.core.designsystem.R as DesignR
import com.wadjet.feature.auth.R
import androidx.compose.ui.res.stringResource
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.wadjet.feature.auth.AuthEvent
import com.wadjet.feature.auth.AuthSheet
import com.wadjet.feature.auth.AuthViewModel
import com.wadjet.feature.auth.sheet.ForgotPasswordSheet
import com.wadjet.feature.auth.sheet.LoginSheet
import com.wadjet.feature.auth.sheet.RegisterSheet
import kotlinx.coroutines.delay
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
    var googleLoading by remember { mutableStateOf(false) }

    // Staggered reveal
    var visibleSections by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        for (i in 1..4) {
            delay(150L)
            visibleSections = i
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.AuthSuccess -> {
                    googleLoading = false
                    onAuthSuccess()
                }
                else -> { googleLoading = false }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WadjetColors.Night),
    ) {
        // Subtle meteor background
        MeteorShower(modifier = Modifier.fillMaxSize())

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
            FadeUp(visible = visibleSections >= 1) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(DesignR.drawable.logo_wadjet),
                        contentDescription = stringResource(R.string.welcome_logo_desc),
                        modifier = Modifier.size(120.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GoldGradientText(
                        text = stringResource(DesignR.string.app_name_display),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.welcome_tagline),
                        style = MaterialTheme.typography.titleLarge,
                        color = WadjetColors.Gold,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Feature preview cards
            FadeUp(visible = visibleSections >= 2) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    FeatureCard("𓂀", stringResource(R.string.welcome_feature_scan_title), stringResource(R.string.welcome_feature_scan_subtitle), Modifier.weight(1f))
                    FeatureCard("𓊹", stringResource(R.string.welcome_feature_dict_title), stringResource(R.string.welcome_feature_dict_subtitle), Modifier.weight(1f))
                    FeatureCard("𓉐", stringResource(R.string.welcome_feature_explore_title), stringResource(R.string.welcome_feature_explore_subtitle), Modifier.weight(1f))
                }
            }

            // Auth buttons
            FadeUp(visible = visibleSections >= 3) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Google Sign-In
                    WadjetGhostButton(
                        text = stringResource(R.string.welcome_sign_in_google),
                        isLoading = googleLoading,
                        onClick = {
                            googleLoading = true
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
                                } catch (e: GetCredentialCancellationException) {
                                    googleLoading = false
                                    Timber.d("Google sign-in cancelled by user")
                                } catch (e: NoCredentialException) {
                                    googleLoading = false
                                    viewModel.onGoogleSignInError(context.getString(R.string.error_no_google_accounts))
                                } catch (e: Exception) {
                                    googleLoading = false
                                    Timber.e(e, "Google sign-in failed")
                                    viewModel.onGoogleSignInError(e.message ?: context.getString(R.string.error_google_sign_in_failed))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Email sign-up
                    WadjetButton(
                        text = stringResource(R.string.welcome_sign_up_email),
                        onClick = { viewModel.showSheet(AuthSheet.REGISTER) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                    )

                    // Already have account
                    androidx.compose.material3.TextButton(
                        onClick = { viewModel.showSheet(AuthSheet.LOGIN) },
                    ) {
                        Text(
                            text = stringResource(R.string.welcome_already_have_account),
                            color = WadjetColors.Sand,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            // Footer
            FadeUp(visible = visibleSections >= 4) {
                Text(
                    text = stringResource(DesignR.string.footer_credit),
                    style = MaterialTheme.typography.bodySmall,
                    color = WadjetColors.Dust,
                )
            }
        }
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
                            } catch (e: GetCredentialCancellationException) {
                                Timber.d("Google sign-in cancelled by user")
                            } catch (e: NoCredentialException) {
                                viewModel.onGoogleSignInError(context.getString(R.string.error_no_google_accounts))
                            } catch (e: Exception) {
                                Timber.e(e, "Google sign-in failed")
                                viewModel.onGoogleSignInError(e.message ?: context.getString(R.string.error_google_sign_in_failed))
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
                            } catch (e: GetCredentialCancellationException) {
                                Timber.d("Google sign-in cancelled by user")
                            } catch (e: NoCredentialException) {
                                viewModel.onGoogleSignInError(context.getString(R.string.error_no_google_accounts))
                            } catch (e: Exception) {
                                Timber.e(e, "Google sign-in failed")
                                viewModel.onGoogleSignInError(e.message ?: context.getString(R.string.error_google_sign_in_failed))
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
private fun FeatureCard(glyph: String, title: String, subtitle: String, modifier: Modifier = Modifier) {
    WadjetCard(
        modifier = modifier.height(120.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = glyph,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = NotoSansEgyptianHieroglyphs,
                color = WadjetColors.Gold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = WadjetColors.Gold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = WadjetColors.TextMuted)
        }
    }
}
