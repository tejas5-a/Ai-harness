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

  @Test
  fun `project file management creation editing and zip export`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = ProjectRepository(context)
    val testProject = repo.createProject("TestProject", com.example.data.model.ProjectType.WEB, "Test description")

    // 1. Create file with spaces and unicode
    val fileName = "my test file 🚀.txt"
    val testContent = "Hello ALTRON CODE AI!\nLines with indentation."
    val created = repo.createFile(testProject, testProject.rootDir, fileName, testContent)
    assertTrue(created.exists())

    // 2. Read back
    val readBack = repo.readFile(testProject, created)
    assertEquals(testContent, readBack)

    // 3. Edit and save
    val updatedContent = testContent + "\nAppended line."
    repo.saveFile(testProject, created, updatedContent)
    assertEquals(updatedContent, repo.readFile(testProject, created))

    // 4. Create directory and rename file
    val folder = repo.createDirectory(testProject, testProject.rootDir, "assets")
    assertTrue(folder.exists() && folder.isDirectory)

    val renamed = repo.renameFile(testProject, created, "renamed_test.txt")
    assertTrue(renamed.exists())
    assertEquals(updatedContent, repo.readFile(testProject, renamed))

    // 5. Generate Project ZIP
    val zip = repo.zipProject(testProject)
    assertTrue(zip.exists())
    assertTrue(zip.length() > 0)
  }
}
