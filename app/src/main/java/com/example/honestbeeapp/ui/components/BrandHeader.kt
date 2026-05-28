package com.example.honestbeeapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BrandHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HonestbeeLogo(modifier = Modifier.size(96.dp))
        Text(
            text = "Fresh food, quick errands, and role-based workspaces.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
