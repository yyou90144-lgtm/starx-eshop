package com.eshop.starx.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eshop.starx.BuildConfig
import com.eshop.starx.core.auth.TokenStore
import com.eshop.starx.data.repository.AuthRepository
import com.eshop.starx.feature.address.AddressEditScreen
import com.eshop.starx.feature.address.AddressListScreen
import com.eshop.starx.feature.auth.LoginScreen
import com.eshop.starx.feature.auth.RegisterScreen
import com.eshop.starx.feature.catalog.ProductDetailScreen
import com.eshop.starx.feature.main.MainShellScreen
import com.eshop.starx.feature.me.HelpScreen
import com.eshop.starx.feature.me.ProfileEditScreen
import kotlinx.coroutines.launch

@Composable
fun StarxNavHost(
    tokenStore: TokenStore,
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository(tokenStore) }
    var bootstrapped by remember { mutableStateOf(false) }
    var startDestination by remember { mutableStateOf(Routes.LOGIN) }
    var sessionTick by remember { mutableIntStateOf(0) }
    var meRefreshKey by remember { mutableIntStateOf(0) }
    var cartRefreshKey by remember { mutableIntStateOf(0) }
    var addressRefreshKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        startDestination = if (tokenStore.hasToken()) Routes.MAIN else Routes.LOGIN
        bootstrapped = true
    }

    if (!bootstrapped) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    sessionTick++
                    cartRefreshKey++
                    meRefreshKey++
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoRegister = { navController.navigate(Routes.REGISTER) },
                onSkipToMainDev = if (BuildConfig.ENABLE_DEV_PREVIEW) {
                    {
                        sessionTick++
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                } else {
                    null
                },
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegistered = {
                    sessionTick++
                    cartRefreshKey++
                    meRefreshKey++
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() },
            )
        }
        composable(Routes.MAIN) {
            @Suppress("UNUSED_EXPRESSION")
            sessionTick
            MainShellScreen(
                tokenStore = tokenStore,
                onLogout = {
                    scope.launch {
                        authRepository.logout()
                        sessionTick++
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.MAIN) { inclusive = true }
                        }
                    }
                },
                onOpenProduct = { productId ->
                    navController.navigate(Routes.productDetail(productId))
                },
                onOpenAddresses = {
                    if (!tokenStore.hasToken()) {
                        navController.navigate(Routes.LOGIN) { launchSingleTop = true }
                    } else {
                        navController.navigate(Routes.ADDRESSES)
                    }
                },
                onOpenProfileEdit = {
                    if (!tokenStore.hasToken()) {
                        navController.navigate(Routes.LOGIN) { launchSingleTop = true }
                    } else {
                        navController.navigate(Routes.PROFILE_EDIT)
                    }
                },
                onOpenHelp = { navController.navigate(Routes.HELP) },
                onNeedLogin = {
                    navController.navigate(Routes.LOGIN) { launchSingleTop = true }
                },
                meRefreshKey = meRefreshKey,
                cartRefreshKey = cartRefreshKey,
            )
        }
        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.LongType }),
        ) { entry ->
            val productId = entry.arguments?.getLong("productId") ?: return@composable
            @Suppress("UNUSED_EXPRESSION")
            sessionTick
            ProductDetailScreen(
                productId = productId,
                hasToken = tokenStore.hasToken(),
                onBack = { navController.popBackStack() },
                onNeedLogin = {
                    navController.navigate(Routes.LOGIN) { launchSingleTop = true }
                },
            )
        }
        composable(Routes.ADDRESSES) {
            AddressListScreen(
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.addressEdit(id)) },
                refreshKey = addressRefreshKey,
            )
        }
        composable(
            route = Routes.ADDRESS_EDIT,
            arguments = listOf(
                navArgument("addressId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) { entry ->
            val rawId = entry.arguments?.getLong("addressId") ?: -1L
            val id = rawId.takeIf { it > 0 }
            AddressEditScreen(
                addressId = id,
                onBack = { navController.popBackStack() },
                onSaved = {
                    addressRefreshKey++
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.PROFILE_EDIT) {
            ProfileEditScreen(
                tokenStore = tokenStore,
                onBack = { navController.popBackStack() },
                onSaved = {
                    meRefreshKey++
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.HELP) {
            HelpScreen(onBack = { navController.popBackStack() })
        }
    }
}
