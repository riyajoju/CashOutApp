package com.xyzbroker.cashoutapp

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.xyzbroker.cashoutapp.presentation.cashout.CashOutRoute
import com.xyzbroker.cashoutapp.presentation.theme.CashOutAppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CashOutAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CashOutRoute(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
