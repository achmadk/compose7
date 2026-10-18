package io.github.compose7.sample.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.templates.LoginTemplate

/** Demonstrates the shipped [LoginTemplate] (PRD §7 "Templates", P2). */
@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    var submittedEmail by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        LoginTemplate(
            onSubmit = { email, _, _ -> submittedEmail = email },
            modifier = Modifier.weight(1f),
            footer = {
                BasicText(
                    text = "Forgot password?",
                    style = Compose7ThemeTypography.labelLarge.copy(color = Compose7ThemeColors.primary),
                )
            },
        )
        submittedEmail?.let { email ->
            DemoCaption(
                text = "Submitted as $email (auth belongs to your app)",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}
