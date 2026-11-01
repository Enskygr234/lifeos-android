package com.lifeos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.app.ui.AppViewModel
import com.lifeos.app.ui.LifeOSRoot
import com.lifeos.app.ui.theme.LifeOSTheme

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by vm.settings.collectAsStateWithLifecycle()
            LifeOSTheme(themeMode = settings.themeMode) {
                LifeOSRoot(vm)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.refreshDay()
    }
}
