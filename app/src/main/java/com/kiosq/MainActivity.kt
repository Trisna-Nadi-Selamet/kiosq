package com.kiosq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kiosq.ui.theme.KiosqTheme

class MainActivity : ComponentActivity() {
    // Lazy-initialize the database
    val database: KiosQDatabase by lazy {
        KiosQDatabase.getInstance(this)
}
override fun onCreate() {
        super.onCreate()
        // Pre-warm DB on background thread
        Thread {
            database.openHelper.writableDatabase
        }.start()
    }

}