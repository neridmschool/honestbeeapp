package com.example.honestbeeapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.honestbeeapp.ui.theme.BeeCream
import com.example.honestbeeapp.ui.theme.BeeDarkText
import com.example.honestbeeapp.ui.theme.BeeError
import com.example.honestbeeapp.ui.theme.BeeHoneyYellow
import com.example.honestbeeapp.ui.theme.BeeMuted
import com.example.honestbeeapp.ui.theme.BeeNavigationSelected
import com.example.honestbeeapp.ui.theme.BeePrimaryYellow
import com.example.honestbeeapp.ui.theme.BeeSuccess
import com.example.honestbeeapp.util.FirebaseConstants

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val colors = statusColors(status)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = colors.background,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ProductCard(
    productName: String,
    price: Double,
    modifier: Modifier = Modifier,
    imageUrl: String = "",
    isAvailable: Boolean = true,
    onAddClick: () -> Unit
) {
    HonestbeeCard(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImagePlaceholder(
                modifier = Modifier.size(78.dp),
                icon = Icons.Outlined.Restaurant,
                contentDescription = if (imageUrl.isBlank()) "$productName placeholder" else "$productName image"
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = productName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatPeso(price),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = BeeDarkText
                )
                if (!isAvailable) {
                    Text(
                        text = "Unavailable",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BeeMuted
                    )
                }
            }

            IconButton(
                onClick = onAddClick,
                enabled = isAvailable,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isAvailable) BeePrimaryYellow else BeeCream,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add $productName",
                    tint = if (isAvailable) BeeDarkText else BeeMuted
                )
            }
        }
    }
}

@Composable
fun StoreCard(
    storeName: String,
    rating: Double,
    deliveryTime: String,
    minimumOrder: Double,
    modifier: Modifier = Modifier,
    imageUrl: String = "",
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            ImagePlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.8f),
                icon = Icons.Outlined.Storefront,
                contentDescription = if (imageUrl.isBlank()) "$storeName placeholder" else "$storeName image"
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = storeName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallMeta(
                    icon = Icons.Outlined.Star,
                    text = String.format("%.1f", rating),
                    iconTint = BeeHoneyYellow
                )
                SmallMeta(
                    icon = Icons.Outlined.LocalShipping,
                    text = deliveryTime
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Minimum ${formatPeso(minimumOrder)}",
                style = MaterialTheme.typography.bodyMedium,
                color = BeeMuted
            )
        }
    }
}

@Composable
fun OrderCard(
    orderNumber: String,
    storeOrCustomerName: String,
    totalAmount: Double,
    status: String,
    modifier: Modifier = Modifier,
    onViewDetailsClick: () -> Unit
) {
    HonestbeeCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImagePlaceholder(
                    modifier = Modifier.size(46.dp),
                    icon = Icons.Outlined.ReceiptLong,
                    contentDescription = "Order"
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = orderNumber,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = storeOrCustomerName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BeeMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusChip(status = status)
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatPeso(totalAmount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = BeeDarkText
                )
                TextButton(onClick = onViewDetailsClick) {
                    Text(
                        text = "View details",
                        color = BeeHoneyYellow,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Image,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    HonestbeeCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ImagePlaceholder(
                modifier = Modifier.size(58.dp),
                icon = icon,
                contentDescription = title
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = BeeMuted
            )
            if (actionText != null && onActionClick != null) {
                HonestbeeOutlinedButton(
                    text = actionText,
                    onClick = onActionClick,
                    fullWidth = false
                )
            }
        }
    }
}

@Composable
private fun ImagePlaceholder(
    modifier: Modifier,
    icon: ImageVector,
    contentDescription: String
) {
    Box(
        modifier = modifier
            .background(
                color = BeeCream,
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = BeeHoneyYellow,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun SmallMeta(
    icon: ImageVector,
    text: String,
    iconTint: Color = BeeMuted
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = BeeMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private data class ChipColors(
    val background: Color,
    val border: Color,
    val text: Color
)

private fun statusColors(status: String): ChipColors {
    return when (status.trim().lowercase()) {
        FirebaseConstants.STATUS_ACTIVE,
        FirebaseConstants.STATUS_APPROVED,
        FirebaseConstants.STATUS_COMPLETED.lowercase(),
        "delivered" -> ChipColors(
            background = BeeSuccess.copy(alpha = 0.12f),
            border = BeeSuccess.copy(alpha = 0.35f),
            text = BeeSuccess
        )

        FirebaseConstants.STATUS_REJECTED,
        FirebaseConstants.STATUS_DELETED,
        FirebaseConstants.STATUS_CANCELLED.lowercase() -> ChipColors(
            background = BeeError.copy(alpha = 0.10f),
            border = BeeError.copy(alpha = 0.30f),
            text = BeeError
        )

        FirebaseConstants.STATUS_PENDING,
        "available",
        "accepted",
        "current",
        "shopping",
        FirebaseConstants.STATUS_TO_PAY.lowercase(),
        FirebaseConstants.STATUS_TO_PREPARE.lowercase(),
        FirebaseConstants.STATUS_TO_SHIP.lowercase(),
        FirebaseConstants.STATUS_OUT_FOR_DELIVERY.lowercase() -> ChipColors(
            background = BeeNavigationSelected,
            border = BeeHoneyYellow.copy(alpha = 0.45f),
            text = BeeDarkText
        )

        else -> ChipColors(
            background = BeeCream,
            border = BeeCream,
            text = BeeMuted
        )
    }
}

private fun formatPeso(amount: Double): String {
    return "PHP " + String.format("%.2f", amount)
}
