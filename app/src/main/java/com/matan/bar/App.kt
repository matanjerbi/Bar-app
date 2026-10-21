package com.matan.bar

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.room.Room
import com.matan.bar.data.BarDatabase
import com.matan.bar.data.Repo
import com.matan.bar.data.SettingsStore
import com.matan.bar.ui.AppNav
import com.matan.bar.ui.BarTheme

class BarApp : Application() {
    lateinit var repo: Repo
        private set
    lateinit var settings: SettingsStore
        private set

    override fun onCreate() {
        super.onCreate()
        repo = Repo(Room.databaseBuilder(this, BarDatabase::class.java, "bar.db").build())
        settings = SettingsStore(this)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            BarTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { AppNav() }
            }
        }
    }
}
