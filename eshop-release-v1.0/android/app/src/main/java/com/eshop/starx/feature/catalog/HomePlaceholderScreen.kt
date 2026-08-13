package com.eshop.starx.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eshop.starx.core.network.NetworkModule
import com.eshop.starx.core.ui.theme.StarxPrimary

@Composable
fun HomePlaceholderScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "星选商城",
            style = MaterialTheme.typography.headlineMedium,
            color = StarxPrimary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "S1 脚手架已就绪",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "下一阶段 S3 将在此实现分类、搜索与商品列表。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "API: ${NetworkModule.apiBaseUrl()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
