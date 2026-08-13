package com.eshop.starx.feature.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eshop.starx.core.auth.TokenStore
import com.eshop.starx.core.navigation.Routes
import com.eshop.starx.core.ui.theme.StarxPrimary
import com.eshop.starx.core.ui.theme.StarxPrimarySoft
import com.eshop.starx.feature.cart.CartScreen
import com.eshop.starx.feature.catalog.HomeScreen
import com.eshop.starx.feature.main.me.MePlaceholderScreen
import com.eshop.starx.feature.order.OrdersPlaceholderScreen

private data class TabItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun MainShellScreen(
    tokenStore: TokenStore,
    onLogout: () -> Unit,
    onOpenProduct: (Long) -> Unit,
    onOpenAddresses: () -> Unit,
    onOpenProfileEdit: () -> Unit,
    onOpenHelp: () -> Unit,
    onNeedLogin: () -> Unit,
    meRefreshKey: Int = 0,
    cartRefreshKey: Int = 0,
    initialTab: String? = null,
) {
    val tabNavController = rememberNavController()
    val tabs = remember {
        listOf(
            TabItem(Routes.TAB_HOME, "首页", Icons.Outlined.Home),
            TabItem(Routes.TAB_CART, "购物车", Icons.Outlined.ShoppingCart),
            TabItem(Routes.TAB_ORDERS, "订单", Icons.AutoMirrored.Outlined.ReceiptLong),
            TabItem(Routes.TAB_ME, "我的", Icons.Outlined.Person),
        )
    }
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(initialTab) {
        val tab = initialTab ?: return@LaunchedEffect
        if (tabs.any { it.route == tab }) {
            tabNavController.navigate(tab) {
                popUpTo(tabNavController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            tabNavController.navigate(tab.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = StarxPrimary,
                            selectedTextColor = StarxPrimary,
                            indicatorColor = StarxPrimarySoft,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = Routes.TAB_HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.TAB_HOME) {
                HomeScreen(onOpenProduct = onOpenProduct)
            }
            composable(Routes.TAB_CART) {
                CartScreen(
                    hasToken = tokenStore.hasToken(),
                    onNeedLogin = onNeedLogin,
                    refreshKey = cartRefreshKey,
                )
            }
            composable(Routes.TAB_ORDERS) {
                OrdersPlaceholderScreen()
            }
            composable(Routes.TAB_ME) {
                MePlaceholderScreen(
                    tokenStore = tokenStore,
                    onLogout = onLogout,
                    onOpenAddresses = onOpenAddresses,
                    onOpenProfileEdit = onOpenProfileEdit,
                    onOpenHelp = onOpenHelp,
                    onOpenOrdersTab = {
                        tabNavController.navigate(Routes.TAB_ORDERS) {
                            popUpTo(tabNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenCartTab = {
                        tabNavController.navigate(Routes.TAB_CART) {
                            popUpTo(tabNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    refreshKey = meRefreshKey,
                )
            }
        }
    }
}
