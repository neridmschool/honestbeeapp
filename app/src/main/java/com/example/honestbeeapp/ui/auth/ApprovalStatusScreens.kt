package com.example.honestbeeapp.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.honestbeeapp.data.model.SessionProfile
import com.example.honestbeeapp.ui.components.HonestbeeButton
import com.example.honestbeeapp.ui.components.HonestbeeCard
import com.example.honestbeeapp.ui.theme.BeeCream
import com.example.honestbeeapp.ui.theme.BeeDarkText
import com.example.honestbeeapp.ui.theme.BeeError
import com.example.honestbeeapp.ui.theme.BeeHoneyYellow
import com.example.honestbeeapp.ui.theme.BeeMuted
import com.example.honestbeeapp.ui.theme.BeePrimaryYellow

@Composable
fun PendingApprovalScreen(
    profile: SessionProfile,
    onLogout: () -> Unit
) {
    ApprovalStateScreen(
        title = "Almost done!",
        message = "Your account is waiting for admin approval.",
        role = profile.role.label,
        icon = Icons.Outlined.CheckCircle,
        iconTint = BeeHoneyYellow,
        onLogout = onLogout
    )
}

@Composable
fun RejectedScreen(
    profile: SessionProfile,
    onLogout: () -> Unit
) {
    ApprovalStateScreen(
        title = "Account rejected",
        message = "Your account application was rejected. Please contact the admin.",
        role = profile.role.label,
        icon = Icons.Outlined.Cancel,
        iconTint = BeeError,
        onLogout = onLogout
    )
}

@Composable
private fun ApprovalStateScreen(
    title: String,
    message: String,
    role: String,
    icon: ImageVector,
    iconTint: Color,
    onLogout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BeeCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HonestbeeCard(
                modifier = Modifier.widthIn(max = 420.dp),
                contentPadding = PaddingValues(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .background(
                                color = BeePrimaryYellow.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(22.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(58.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, BeePrimaryYellow.copy(alpha = 0.65f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = BeeDarkText,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BeeMuted,
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = BeePrimaryYellow.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, BeePrimaryYellow.copy(alpha = 0.55f))
                    ) {
                        Text(
                            text = role,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = BeeDarkText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    HonestbeeButton(
                        text = "Logout",
                        onClick = onLogout
                    )
                }
            }
        }
    }
}
