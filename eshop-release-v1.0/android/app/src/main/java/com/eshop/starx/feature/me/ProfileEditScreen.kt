package com.eshop.starx.feature.me

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eshop.starx.core.auth.TokenStore
import com.eshop.starx.data.repository.AuthRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    tokenStore: TokenStore,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val repo = remember { AuthRepository(tokenStore) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var nickname by remember { mutableStateOf(tokenStore.getNickname().orEmpty()) }
    var phone by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loading = true
        try {
            val me = repo.me()
            nickname = me.nickname?.takeIf { it.isNotBlank() } ?: me.username
            phone = me.phone.orEmpty()
        } catch (e: Exception) {
            snackbar.showSnackbar(e.message ?: "加载失败")
        } finally {
            loading = false
        }
    }

    fun canSave(): Boolean {
        val n = nickname.trim()
        val p = phone.trim()
        if (n.isEmpty() || n.length > 50) return false
        if (p.isNotEmpty() && !Regex("^[0-9+\\- ]{6,20}$").matches(p)) return false
        return !saving && !loading
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑资料") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { if (it.length <= 50) nickname = it },
                    label = { Text("昵称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${nickname.trim().length}/50") },
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 20) phone = it },
                    label = { Text("联系电话") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("选填，6-20 位数字/空格/+/-") },
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            saving = true
                            try {
                                repo.updateProfile(nickname.trim(), phone.trim().ifBlank { null })
                                snackbar.showSnackbar("已保存")
                                onSaved()
                            } catch (e: Exception) {
                                snackbar.showSnackbar(e.message ?: "保存失败")
                            } finally {
                                saving = false
                            }
                        }
                    },
                    enabled = canSave(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(if (saving) "保存中..." else "保存修改")
                }
            }
        }
    }
}
