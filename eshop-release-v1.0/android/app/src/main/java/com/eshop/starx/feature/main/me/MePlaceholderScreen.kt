package com.eshop.starx.feature.main.me

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eshop.starx.core.auth.TokenStore
import com.eshop.starx.core.ui.theme.StarxBorder
import com.eshop.starx.core.ui.theme.StarxInk
import com.eshop.starx.core.ui.theme.StarxMuted
import com.eshop.starx.core.ui.theme.StarxPage
import com.eshop.starx.core.ui.theme.StarxPrimary
import com.eshop.starx.core.ui.theme.StarxPrimarySoft
import com.eshop.starx.core.ui.theme.StarxPrimaryStrong
import com.eshop.starx.core.ui.theme.StarxSurface
import com.eshop.starx.core.ui.theme.StarxTextSoft
import com.eshop.starx.data.model.CurrentUserResponse
import com.eshop.starx.data.repository.AuthRepository

@Composable
fun MePlaceholderScreen(
    tokenStore: TokenStore,
    onLogout: () -> Unit,
    onOpenAddresses: () -> Unit = {},
    onOpenProfileEdit: () -> Unit = {},
    onOpenHelp: () -> Unit = {},
    onOpenOrdersTab: () -> Unit = {},
    onOpenCartTab: () -> Unit = {},
    refreshKey: Int = 0,
) {
    val authRepository = remember { AuthRepository(tokenStore) }
    val hasToken = tokenStore.hasToken()
    val localName = tokenStore.getNickname()?.takeIf { it.isNotBlank() }
        ?: tokenStore.getUsername()
        ?: "星选用户"
    val localUsername = tokenStore.getUsername()

    var loading by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf<CurrentUserResponse?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(hasToken, refreshKey) {
        if (!hasToken) {
            profile = null
            loadError = null
            return@LaunchedEffect
        }
        loading = true
        loadError = null
        try {
            profile = authRepository.me()
        } catch (e: Exception) {
            loadError = e.message ?: "资料加载失败"
            profile = null
        } finally {
            loading = false
        }
    }

    val displayName = profile?.nickname?.takeIf { it.isNotBlank() }
        ?: profile?.username?.takeIf { it.isNotBlank() }
        ?: localName
    val username = profile?.username?.takeIf { it.isNotBlank() } ?: localUsername
    val phone = profile?.phone?.takeIf { it.isNotBlank() }
    val roleLabel = when (profile?.role?.uppercase()) {
        "ADMIN" -> "平台管理员"
        "SELLER" -> "商家"
        "USER", null -> if (hasToken) "买家会员" else "访客"
        else -> profile?.role ?: "买家会员"
    }
    val avatarLetter = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "星"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StarxPage)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF5F4), StarxPage),
                    ),
                )
                .padding(horizontal = 20.dp, vertical = 22.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "我的",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                    ),
                    color = StarxInk,
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = StarxSurface,
                    shadowElevation = 2.dp,
                    tonalElevation = 0.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(StarxPrimary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = avatarLetter,
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = StarxInk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (!username.isNullOrBlank()) {
                                Text(
                                    text = "@$username",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StarxMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            } else if (!hasToken) {
                                Text(
                                    text = "登录后同步订单与地址",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StarxMuted,
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            RoleChip(text = roleLabel, loggedIn = hasToken)
                        }
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = StarxPrimary,
                            )
                        }
                    }
                }

                if (loadError != null && hasToken) {
                    Text(
                        text = loadError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = StarxPrimaryStrong,
                    )
                }
            }
        }

        SectionCard(title = "账户信息") {
            InfoRow(label = "昵称", value = displayName)
            HorizontalDivider(color = StarxBorder, thickness = 0.5.dp)
            InfoRow(label = "用户名", value = username ?: if (hasToken) "—" else "未登录")
            HorizontalDivider(color = StarxBorder, thickness = 0.5.dp)
            InfoRow(
                label = "手机号",
                value = phone ?: if (hasToken) "未绑定" else "登录后可查看",
            )
            HorizontalDivider(color = StarxBorder, thickness = 0.5.dp)
            InfoRow(label = "角色", value = roleLabel)
        }

        SectionCard(title = "我的服务") {
            ServiceRow(
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                title = "我的订单",
                subtitle = "查看订单进度（完整支付闭环持续完善）",
                enabled = true,
                trailing = "去查看",
                onClick = onOpenOrdersTab,
            )
            HorizontalDivider(color = StarxBorder, thickness = 0.5.dp)
            ServiceRow(
                icon = Icons.Outlined.ShoppingCart,
                title = "购物车",
                subtitle = "改数量、删除已加购商品",
                enabled = true,
                trailing = "去看看",
                onClick = onOpenCartTab,
            )
            HorizontalDivider(color = StarxBorder, thickness = 0.5.dp)
            ServiceRow(
                icon = Icons.Outlined.LocationOn,
                title = "收货地址",
                subtitle = "新增 / 编辑 / 默认 / 删除",
                enabled = hasToken,
                trailing = if (hasToken) "管理" else "需登录",
                onClick = onOpenAddresses,
            )
            HorizontalDivider(color = StarxBorder, thickness = 0.5.dp)
            ServiceRow(
                icon = Icons.Outlined.Person,
                title = "个人资料编辑",
                subtitle = "修改昵称与联系电话",
                enabled = hasToken,
                trailing = if (hasToken) "编辑" else "需登录",
                onClick = onOpenProfileEdit,
            )
            HorizontalDivider(color = StarxBorder, thickness = 0.5.dp)
            ServiceRow(
                icon = Icons.Outlined.SupportAgent,
                title = "帮助与客服",
                subtitle = "常见问题与使用说明",
                enabled = true,
                trailing = "查看",
                onClick = onOpenHelp,
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = StarxPrimarySoft,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = null,
                    tint = StarxPrimary,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "星选 STARX",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = StarxPrimaryStrong,
                    )
                    Text(
                        text = "原生客户端 · 品质生活一站购",
                        style = MaterialTheme.typography.bodySmall,
                        color = StarxTextSoft,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (hasToken) {
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StarxPrimary,
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("退出登录", fontWeight = FontWeight.SemiBold)
            }
        } else {
            Text(
                text = "当前为访客预览。登录后可同步身份信息并完成加购。",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = StarxMuted,
            )
            TextButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) {
                Text("返回登录", color = StarxPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RoleChip(text: String, loggedIn: Boolean) {
    val bg = if (loggedIn) StarxPrimarySoft else Color(0xFFF0F1F3)
    val fg = if (loggedIn) StarxPrimaryStrong else StarxMuted
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = StarxInk,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = StarxSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, StarxBorder),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = StarxMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = StarxInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Composable
private fun ServiceRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    trailing: String,
    onClick: () -> Unit,
) {
    val titleColor = if (enabled) StarxInk else StarxMuted
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (enabled) StarxPrimarySoft else Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) StarxPrimary else StarxMuted,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = titleColor,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = StarxMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = trailing,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) StarxPrimary else StarxMuted,
        )
        if (enabled) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = StarxMuted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
