package com.example.honestbeeapp.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.honestbeeapp.ui.components.HonestbeeButton
import com.example.honestbeeapp.ui.components.HonestbeeCard
import com.example.honestbeeapp.ui.components.HonestbeeLogo
import com.example.honestbeeapp.ui.components.HonestbeeOutlinedButton
import com.example.honestbeeapp.ui.theme.BeeCream
import com.example.honestbeeapp.ui.theme.BeeDarkText
import com.example.honestbeeapp.ui.theme.BeeMuted

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onCustomerSignUpClick: () -> Unit,
    onMerchantSignUpClick: () -> Unit,
    onRiderSignUpClick: () -> Unit
) {
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
                .padding(horizontal = 16.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HonestbeeCard(modifier = Modifier.widthIn(max = 430.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HonestbeeLogo(modifier = Modifier.size(108.dp))
                    Text(
                        text = "honestbee",
                        style = MaterialTheme.typography.headlineSmall,
                        color = BeeDarkText,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Groceries and food delivered fast",
                        style = MaterialTheme.typography.titleLarge,
                        color = BeeDarkText,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Shop from stores, track orders, and manage deliveries in one app.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BeeMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(4.dp))

                    HonestbeeButton(
                        text = "Login",
                        onClick = onLoginClick
                    )
                    HonestbeeOutlinedButton(
                        text = "Sign Up as Customer",
                        onClick = onCustomerSignUpClick
                    )
                    HonestbeeOutlinedButton(
                        text = "Become a Merchant",
                        onClick = onMerchantSignUpClick
                    )
                    HonestbeeOutlinedButton(
                        text = "Apply as Rider",
                        onClick = onRiderSignUpClick
                    )
                }
            }
        }
    }
}
