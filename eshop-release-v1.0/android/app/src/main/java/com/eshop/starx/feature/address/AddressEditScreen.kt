package com.eshop.starx.feature.address

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eshop.starx.data.model.AddressRequest
import com.eshop.starx.data.repository.AddressRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressEditScreen(
    addressId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val isNew = addressId == null || addressId <= 0
    val repo = remember { AddressRepository() }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var receiverName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(!isNew) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(addressId) {
        if (isNew) return@LaunchedEffect
        loading = true
        try {
            val list = repo.list()
            val item = list.firstOrNull { it.id == addressId }
            if (item != null) {
                receiverName = item.receiverName
                phone = item.phone
                province = item.province
                city = item.city
                district = item.district
                detail = item.detail
                isDefault = item.isDefault == true
            } else {
                snackbar.showSnackbar("地址不存在")
            }
        } catch (e: Exception) {
            snackbar.showSnackbar(e.message ?: "加载失败")
        } finally {
            loading = false
        }
    }

    fun valid(): Boolean {
        if (receiverName.isBlank() || phone.isBlank()) return false
        if (!phone.matches(Regex("^[0-9+\\- ]{6,20}$"))) return false
        if (province.isBlank() || city.isBlank() || district.isBlank() || detail.isBlank()) return false
        return !saving && !loading
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "新增地址" else "编辑地址") },
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
            Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(receiverName, { if (it.length <= 50) receiverName = it }, label = { Text("收货人") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(phone, { if (it.length <= 20) phone = it }, label = { Text("联系电话") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(province, { if (it.length <= 50) province = it }, label = { Text("省份") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(city, { if (it.length <= 50) city = it }, label = { Text("城市") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(district, { if (it.length <= 50) district = it }, label = { Text("区县") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(detail, { if (it.length <= 255) detail = it }, label = { Text("详细地址") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                Text("设为默认地址")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        saving = true
                        try {
                            val body = AddressRequest(
                                receiverName = receiverName.trim(),
                                phone = phone.trim(),
                                province = province.trim(),
                                city = city.trim(),
                                district = district.trim(),
                                detail = detail.trim(),
                                isDefault = isDefault,
                            )
                            if (isNew) repo.create(body) else repo.update(addressId!!, body)
                            snackbar.showSnackbar("已保存")
                            onSaved()
                        } catch (e: Exception) {
                            snackbar.showSnackbar(e.message ?: "保存失败")
                        } finally {
                            saving = false
                        }
                    }
                },
                enabled = valid(),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(if (saving) "保存中..." else "保存")
            }
        }
    }
}
