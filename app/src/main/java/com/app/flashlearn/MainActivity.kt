package com.app.flashlearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.app.flashlearn.navigation.FlashLearnApp
import com.app.flashlearn.ui.theme.FlashLearnTheme

// نکته: isFirstLaunch باید از AppSettingsRepository/DataStore واقعی خونده بشه.
// اینجا فقط اسکلت گذاشته شده تا کامپایل بشه.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashLearnTheme {
                FlashLearnApp(isFirstLaunch = false) // TODO: از تنظیمات واقعی بخون
            }
        }
    }
}
