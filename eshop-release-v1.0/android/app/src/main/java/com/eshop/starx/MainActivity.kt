package com.eshop.starx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.eshop.starx.core.navigation.StarxNavHost
import com.eshop.starx.core.ui.theme.StarxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StarxTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StarxNavHost(
                        tokenStore = StarxApp.instance.tokenStore,
                    )
                }
            }
        }
    }
}
