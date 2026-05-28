package com.example.honestbeeapp.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import com.example.honestbeeapp.ui.components.ErrorMessage
import com.example.honestbeeapp.ui.components.HonestbeeLogo
import com.example.honestbeeapp.ui.components.HonestbeeButton
import com.example.honestbeeapp.ui.components.HonestbeeCard
import com.example.honestbeeapp.ui.components.HonestbeePasswordField
import com.example.honestbeeapp.ui.components.HonestbeeTextField
import com.example.honestbeeapp.ui.theme.BeeCream
import com.example.honestbeeapp.ui.theme.BeeDarkText
import com.example.honestbeeapp.ui.theme.BeeHoneyYellow
import com.example.honestbeeapp.ui.theme.BeeMuted
import com.example.honestbeeapp.ui.theme.BeePrimaryYellow
import com.example.honestbeeapp.ui.theme.BeeSuccess
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    onBackToWelcome: () -> Unit,
    initialMessage: String? = null,
    auth: FirebaseAuth = remember { FirebaseAuth.getInstance() }
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var rememberMe by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isMessageError by remember { mutableStateOf(true) }

    BackHandler(onBack = onBackToWelcome)

    LaunchedEffect(initialMessage) {
        if (!initialMessage.isNullOrBlank()) {
            isMessageError = true
            message = initialMessage
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BeeCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(12.dp))
            LoginLogo()
            Spacer(Modifier.height(18.dp))

            HonestbeeCard(
                modifier = Modifier.widthIn(max = 430.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome back!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = BeeDarkText,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Login to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BeeMuted,
                        textAlign = TextAlign.Center
                    )

                    HonestbeeTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Email address",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        enabled = !isLoading
                    )

                    HonestbeePasswordField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Password",
                        leadingIcon = Icons.Outlined.Lock,
                        enabled = !isLoading
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                enabled = !isLoading,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = BeePrimaryYellow,
                                    checkmarkColor = BeeDarkText
                                )
                            )
                            Text(
                                text = "Remember me",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BeeDarkText
                            )
                        }

                        TextButton(
                            onClick = {
                                val cleanEmail = email.trim()
                                if (cleanEmail.isBlank()) {
                                    isMessageError = true
                                    message = "Enter your email first."
                                    return@TextButton
                                }
                                auth.sendPasswordResetEmail(cleanEmail)
                                    .addOnSuccessListener {
                                        isMessageError = false
                                        message = "Password reset email sent."
                                    }
                                    .addOnFailureListener {
                                        isMessageError = true
                                        message = it.localizedMessage ?: "Could not send reset email."
                                    }
                            },
                            enabled = !isLoading
                        ) {
                            Text(
                                text = "Forgot password?",
                                color = BeeHoneyYellow,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    message?.let {
                        if (isMessageError) {
                            ErrorMessage(it)
                        } else {
                            LoginMessage(it)
                        }
                    }

                    HonestbeeButton(
                        text = "Login",
                        isLoading = isLoading,
                        enabled = email.isNotBlank() && password.isNotBlank(),
                        onClick = {
                            val cleanEmail = email.trim()
                            if (!cleanEmail.contains("@") || password.length < 6) {
                                isMessageError = true
                                message = "Enter a valid email and password."
                                return@HonestbeeButton
                            }

                            isLoading = true
                            message = null
                            auth.signInWithEmailAndPassword(cleanEmail, password)
                                .addOnSuccessListener {
                                    isLoading = false
                                    onSignedIn()
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    isMessageError = true
                                    message = it.localizedMessage ?: "Login failed."
                                }
                        }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Not ready to log in?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BeeMuted
                        )
                        TextButton(onClick = onBackToWelcome) {
                            Text(
                                text = "Back to welcome",
                                color = BeeHoneyYellow,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            DeliveryPlaceholder()
        }
    }
}

@Composable
private fun LoginMessage(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = BeeSuccess.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, BeeSuccess.copy(alpha = 0.35f))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = BeeSuccess,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun LoginLogo() {
    HonestbeeLogo(modifier = Modifier.size(104.dp))
}

@Composable
private fun DeliveryPlaceholder() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 430.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalShipping,
                    contentDescription = null,
                    tint = BeeHoneyYellow,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column {
                Text(
                    text = "Fresh picks, fast delivery",
                    style = MaterialTheme.typography.titleMedium,
                    color = BeeDarkText
                )
                Text(
                    text = "Your grocery run starts here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BeeMuted
                )
            }
        }
    }
}
