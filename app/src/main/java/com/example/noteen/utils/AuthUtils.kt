package com.example.noteen.utils

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.authenticateAndOpenLockedFolder(
    title: String = "",
    subTitle: String = "",
    onSuccess: () -> Unit,
    onFail: () -> Unit
) {
    val executor = ContextCompat.getMainExecutor(this)

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subTitle)
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()

    BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) =
            onSuccess()

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) =
            onFail()

        override fun onAuthenticationFailed() = onFail()
    }).authenticate(promptInfo)
}
