package com.eshop.starx.feature.address

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eshop.starx.core.ui.theme.StarxBorder
import com.eshop.starx.core.ui.theme.StarxInk
import com.eshop.starx.core.ui.theme.StarxMuted
import com.eshop.starx.core.ui.theme.StarxPrimary
import com.eshop.starx.core.ui.theme.StarxPrimarySoft
import com.eshop.starx.core.ui.theme.StarxSurface
import com.eshop.starx.data.model.UserAddressDto
import com.eshop.starx.data.repository.AddressRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressListScreen(
    onBack: () -> Unit,
    onEdit: (Long?) -> Unit,
    refreshKey: Int = 0,
) {
    val repo = remember { AddressRepository() }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var loading by remember { mutableStateOf(true) }
    var items by remember { mutableStateOf<List<UserAddressDto>>(emptyList()) }

    fun reload() {
        scope.launch {
            loading = true
            try {
                items = repo.list()
            } catch (e: Exception) {
                snackbar.showSnackbar(e.message ?: "加载失败")
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(refreshKey) { reload() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("收货地址") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEdit(null) },
                containerColor = StarxPrimary,
                contentColor = Color.White,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "新增地址")
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        when {
            loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StarxPrimary)
            }
            items.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("暂无收货地址", color = StarxMuted)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { onEdit(null) }) { Text("新增地址") }
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items, key = { it.id }) { addr ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = StarxSurface,
                        border = BorderStroke(1.dp, StarxBorder),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(addr.receiverName, fontWeight = FontWeight.SemiBold, color = StarxInk)
                                Spacer(Modifier.width(10.dp))
                                Text(addr.phone, color = StarxMuted, style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.weight(1f))
                                if (addr.isDefault == true) {
                                    Surface(color = StarxPrimarySoft, shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            "默认",
                                            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            color = StarxPrimary,
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }
                            }
                            Text(
                                "${addr.province}${addr.city}${addr.district}${addr.detail}",
                                color = StarxInk,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Row {
                                TextButton(onClick = { onEdit(addr.id) }) { Text("编辑") }
                                if (addr.isDefault != true) {
                                    TextButton(onClick = {
                                        scope.launch {
                                            try {
                                                repo.setDefault(addr.id)
                                                reload()
                                            } catch (e: Exception) {
                                                snackbar.showSnackbar(e.message ?: "设置失败")
                                            }
                                        }
                                    }) { Text("设为默认") }
                                }
                                TextButton(onClick = {
                                    scope.launch {
                                        try {
                                            repo.delete(addr.id)
                                            reload()
                                        } catch (e: Exception) {
                                            snackbar.showSnackbar(e.message ?: "删除失败")
                                        }
                                    }
                                }) { Text("删除", color = StarxPrimary) }
                            }
                        }
                    }
                }
            }
        }
    }
}
