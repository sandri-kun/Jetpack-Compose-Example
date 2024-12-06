package org.jetpack.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.jetpack.compose.screens.CrashScreen
import org.jetpack.compose.ui.theme.MyAppTheme

class DebugActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val errorMessage = intent.getStringExtra("error") ?: "No error available."

        setContent {
            MyAppTheme {
                CrashScreen(errorMessage)
            }
        }
    }
}