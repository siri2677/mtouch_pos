package com.example.mtouchpos.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.mtouchpos.view.navgraph.CommonViewNavGraph
import com.example.mtouchpos.view.navgraph.MainViewNavGraph
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.theme.MtouchPos
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreditPaymentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MtouchPos {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val resultProcess: (Int, Intent) -> Unit = { int, intent ->
                        setResult(int, intent)
                        finish()
                    }
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = NavigationGraphState.HomeView.Home.name
                    ) {
                        with(MainViewNavGraph(CommonViewNavGraph(navController, this))) {
                            creditPaymentGraph()
                        }
                    }
                }
            }
        }
    }
}