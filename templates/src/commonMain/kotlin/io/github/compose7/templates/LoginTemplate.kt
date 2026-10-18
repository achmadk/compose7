package io.github.compose7.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.compose7.components.buttons.Button
import io.github.compose7.components.forms.Checkbox
import io.github.compose7.components.forms.TextField
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.Block
import io.github.compose7.foundation.Card
import io.github.compose7.foundation.Page

/**
 * Login screen template (PRD §7 "Templates", P2).
 *
 * A complete, accessible sign-in screen built only from library components -
 * the caller owns authentication and receives the submitted values. Field
 * errors are local (empty / malformed), never pretending to be server feedback.
 *
 * ```kotlin
 * LoginTemplate(onSubmit = { email, password, remember ->
 *     viewModel.signIn(email, password, remember)
 * })
 * ```
 *
 * @param logo Optional brand mark above the title (no bundled icons, PRD §4).
 * @param footer Extra slot under the form (forgot password, sign-up link).
 */
@Composable
fun LoginTemplate(
    onSubmit: (email: String, password: String, rememberMe: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Welcome back",
    subtitle: String = "Sign in to continue",
    logo: (@Composable () -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var rememberMe by rememberSaveable { mutableStateOf(true) }
    var submitted by remember { mutableStateOf(false) }

    val emailError = submitted && !email.looksLikeEmail()
    val passwordError = submitted && password.isEmpty()
    val canSubmit = email.isNotBlank() && password.isNotBlank()

    val emailSupport: (@Composable () -> Unit)? = if (emailError) {
        { BasicText(text = "Enter a valid email address", style = type.bodySmall) }
    } else {
        null
    }
    val passwordSupport: (@Composable () -> Unit)? = if (passwordError) {
        { BasicText(text = "Password is required", style = type.bodySmall) }
    } else {
        null
    }

    Page(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            logo?.invoke()
            Spacer(Modifier.height(if (logo != null) 20.dp else 0.dp))
            BasicText(
                text = title,
                style = type.headlineSmall.copy(color = colors.onSurface),
            )
            Spacer(Modifier.height(6.dp))
            BasicText(
                text = subtitle,
                style = type.bodyMedium.copy(color = colors.onSurfaceVariant),
            )
            Spacer(Modifier.height(24.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Block {
                    TextField(
                        value = email,
                        onValueChange = {
                            email = it
                            submitted = false
                        },
                        label = "Email",
                        placeholder = "you@example.com",
                        isError = emailError,
                        supportingText = emailSupport,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = password,
                        onValueChange = {
                            password = it
                            submitted = false
                        },
                        label = "Password",
                        isError = passwordError,
                        supportingText = passwordSupport,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            label = "Remember me",
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            submitted = true
                            if (canSubmit && email.looksLikeEmail()) {
                                onSubmit(email.trim(), password, rememberMe)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canSubmit,
                        text = "Sign in",
                    )
                }
            }
            if (footer != null) {
                Spacer(Modifier.height(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) { footer() }
            }
        }
    }
}

/** Intentionally permissive: enough to catch typos, not a validator library. */
private fun String.looksLikeEmail(): Boolean {
    val at = indexOf('@')
    val dot = lastIndexOf('.')
    return at in 1 until length - 1 && dot > at + 1 && dot < length - 1 && !contains(' ')
}
