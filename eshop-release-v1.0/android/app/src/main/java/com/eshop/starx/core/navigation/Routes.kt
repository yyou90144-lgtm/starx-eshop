package com.eshop.starx.core.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"

    const val TAB_HOME = "tab_home"
    const val TAB_CART = "tab_cart"
    const val TAB_ORDERS = "tab_orders"
    const val TAB_ME = "tab_me"

    const val PRODUCT_DETAIL = "product/{productId}"
    fun productDetail(productId: Long) = "product/$productId"

    const val CHECKOUT = "checkout"
    const val PAYMENT = "payment/{orderId}"
    fun payment(orderId: Long) = "payment/$orderId"

    const val ORDER_DETAIL = "order/{orderId}"
    fun orderDetail(orderId: Long) = "order/$orderId"

    const val ADDRESSES = "addresses"
    const val ADDRESS_EDIT = "address-edit/{addressId}"
    fun addressEdit(addressId: Long? = null): String =
        "address-edit/${addressId ?: -1L}"

    const val PROFILE_EDIT = "profile-edit"
    const val HELP = "help"
}
