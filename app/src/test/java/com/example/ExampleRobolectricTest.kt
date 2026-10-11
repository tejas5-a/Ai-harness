package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.ProjectRepository
import com.example.data.repository.TerminalManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Altron Code", appName)
  }

  @Test
  fun `project repository initializes default workspaces`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = ProjectRepository(context)
    val projects = repo.getProjects()
    assertTrue(projects.isNotEmpty())
  }

  @Test
  fun `terminal manager detects dangerous destructive commands`() {
    val terminal = TerminalManager()
    assertTrue(terminal.isPotentiallyDangerous("rm -rf /"))
    assertTrue(terminal.isPotentiallyDangerous("rm -r ./app"))
    assertTrue(terminal.isPotentiallyDangerous("mkfs.ext4 /dev/block"))
    assertFalse(terminal.isPotentiallyDangerous("ls -la"))
    assertFalse(terminal.isPotentiallyDangerous("pwd"))
    assertFalse(terminal.isPotentiallyDangerous("cat README.md"))
  }

  @Test
  fun `tactile feedback helper executes safely`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    // Ensure invoking triggerTactileFeedback with null haptic does not crash
    com.example.ui.components.triggerTactileFeedback(context, null)
  }
}
