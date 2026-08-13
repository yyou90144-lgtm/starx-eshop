package com.eshop.starx.feature.me

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eshop.starx.core.ui.theme.StarxInk
import com.eshop.starx.core.ui.theme.StarxMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    val items = listOf(
        "如何登录？" to "使用买家账号登录；性能包可用「开发预览」先浏览商品。",
        "如何加购？" to "商品详情选规格与数量后点「加入购物车」。未登录会引导登录。",
        "购物车？" to "底部 Tab「购物车」可改数量、删除。",
        "收货地址？" to "「我的」→「收货地址」新增/编辑/设默认/删除。",
        "资料修改？" to "「我的」→「个人资料编辑」修改昵称与手机号。",
        "客服？" to "课程演示版无真实客服，请联系小组或教师。",
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("帮助与客服") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("常见问题", style = MaterialTheme.typography.titleMedium, color = StarxInk)
            items.forEach { (q, a) ->
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(q, style = MaterialTheme.typography.titleSmall, color = StarxInk)
                    Text(a, style = MaterialTheme.typography.bodyMedium, color = StarxMuted)
                }
                HorizontalDivider()
            }
        }
    }
}
