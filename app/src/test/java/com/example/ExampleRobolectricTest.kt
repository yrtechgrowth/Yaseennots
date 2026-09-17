package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppSettings
import com.example.data.SettingsManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Yaseen", appName)
  }

  @Test
  fun `settings manager saves and loads custom app name and icon theme`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val settingsManager = SettingsManager(context)
    val initial = settingsManager.loadSettings()
    assertNotNull(initial)
    assertEquals("Yaseen", initial.appName)

    val updated = initial.copy(
      appName = "Yaseen Suite Pro",
      iconTheme = "Neon Cyan",
      aiProvider = "OPENAI",
      openAiApiKey = "sk-test-key-12345"
    )
    settingsManager.saveSettings(updated)

    val reloaded = settingsManager.loadSettings()
    assertEquals("Yaseen Suite Pro", reloaded.appName)
    assertEquals("Neon Cyan", reloaded.iconTheme)
    assertEquals("OPENAI", reloaded.aiProvider)
    assertEquals("sk-test-key-12345", reloaded.getActiveApiKey())
  }
}

