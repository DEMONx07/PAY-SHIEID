package com.payshield.app.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.payshield.app.domain.model.RiskResult
import com.payshield.app.security.engine.PayShieldSecurityEngine
import com.payshield.app.ui.screens.*
import com.payshield.app.ui.theme.PayShieldTheme

enum class Screen {
    HOME,
    QR_SCAN,
    MESSAGE_CHECK,
    RISK_RESULT
}

@Composable
fun PayShieldApp(
    sharedText: String? = null
) {
    val context = LocalContext.current
    val securityEngine = remember { PayShieldSecurityEngine.getInstance(context) }

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var currentRiskResult by remember { mutableStateOf<RiskResult?>(null) }
    var pendingMessageText by remember { mutableStateOf(sharedText ?: "") }

    LaunchedEffect(sharedText) {
        if (!sharedText.isNull_or_blank()) {
            pendingMessageText = sharedText.orEmpty()
            currentScreen = Screen.MESSAGE_CHECK
        }
    }

    PayShieldTheme {
        when (currentScreen) {
            Screen.HOME -> HomeScreen(
                onNavigateToQrScan = { currentScreen = Screen.QR_SCAN },
                onNavigateToMessageCheck = { currentScreen = Screen.MESSAGE_CHECK }
            )
            Screen.QR_SCAN -> QrScanScreen(
                onQrPayloadScanned = { payload ->
                    val result = securityEngine.analyzeQrPayload(payload)
                    currentRiskResult = result
                    currentScreen = Screen.RISK_RESULT
                },
                onNavigateBack = { currentScreen = Screen.HOME }
            )
            Screen.MESSAGE_CHECK -> MessageCheckScreen(
                initialText = pendingMessageText,
                onAnalyzeMessage = { text ->
                    val result = securityEngine.analyzeMessageText(text)
                    currentRiskResult = result
                    currentScreen = Screen.RISK_RESULT
                },
                onNavigateBack = { currentScreen = Screen.HOME }
            )
            Screen.RISK_RESULT -> {
                val result = currentRiskResult
                if (result != null) {
                    RiskResultScreen(
                        result = result,
                        onNavigateBack = { currentScreen = Screen.HOME }
                    )
                } else {
                    currentScreen = Screen.HOME
                }
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
