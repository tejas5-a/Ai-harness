package com.example.data.repository

import android.content.Context
import com.example.data.model.Project
import com.example.data.model.ProjectFile
import com.example.data.model.ProjectType
import java.io.File

class ProjectRepository(private val context: Context) {

  private val workspaceDir: File by lazy {
    File(context.filesDir, "altron_workspace").apply {
      if (!exists()) mkdirs()
    }
  }

  init {
    ensureDefaultProjects()
  }

  fun getProjects(): List<Project> {
    val dirs = workspaceDir.listFiles { file -> file.isDirectory } ?: emptyArray()
    val projects = dirs.map { dir ->
      val metaFile = File(dir, ".altron_meta")
      val type = if (metaFile.exists()) {
        try {
          val typeName = metaFile.readLines().firstOrNull() ?: ProjectType.CUSTOM.name
          ProjectType.valueOf(typeName)
        } catch (e: Exception) {
          ProjectType.CUSTOM
        }
      } else {
        detectProjectType(dir)
      }

      val desc = if (metaFile.exists()) {
        metaFile.readLines().getOrNull(1) ?: "ALTRON Project"
      } else {
        "Local Android Workspace Project"
      }

      Project(
        id = dir.name,
        name = dir.name.replace("_", " "),
        description = desc,
        type = type,
        rootDir = dir,
        createdAt = dir.lastModified(),
        lastModified = dir.lastModified()
      )
    }

    return if (projects.isEmpty()) {
      ensureDefaultProjects()
      getProjects()
    } else {
      projects.sortedByDescending { it.lastModified }
    }
  }

  fun getProjectById(projectId: String): Project? {
    return getProjects().find { it.id == projectId }
  }

  fun createProject(name: String, type: ProjectType, description: String): Project {
    val sanitized = name.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_").ifEmpty { "project_${System.currentTimeMillis()}" }
    val projectDir = File(workspaceDir, sanitized)
    if (!projectDir.exists()) {
      projectDir.mkdirs()
    }

    // Save metadata
    val metaFile = File(projectDir, ".altron_meta")
    metaFile.writeText("${type.name}\n$description\n")

    // Populate initial template files
    when (type) {
      ProjectType.ANDROID -> {
        val srcDir = File(projectDir, "app/src/main/java/com/app").apply { mkdirs() }
        File(srcDir, "MainActivity.kt").writeText(
          """package com.app
            |
            |import android.os.Bundle
            |import androidx.activity.ComponentActivity
            |import androidx.activity.compose.setContent
            |import androidx.compose.material3.Text
            |import androidx.compose.runtime.Composable
            |
            |class MainActivity : ComponentActivity() {
            |    override fun onCreate(savedInstanceState: Bundle?) {
            |        super.onCreate(savedInstanceState)
            |        setContent {
            |            Text("Hello from $name on Android!")
            |        }
            |    }
            |}
          """.trimMargin()
        )
        File(projectDir, "build.gradle.kts").writeText(
          """plugins {
            |    id("com.android.application")
            |    kotlin("android")
            |}
            |
            |android {
            |    namespace = "com.app"
            |    compileSdk = 34
            |}
          """.trimMargin()
        )
        File(projectDir, "README.md").writeText("# $name\n\nAndroid Jetpack Compose project created in ALTRON CODE AI.")
      }

      ProjectType.WEB -> {
        File(projectDir, "index.html").writeText(
          """<!DOCTYPE html>
            |<html lang="en">
            |<head>
            |    <meta charset="UTF-8">
            |    <meta name="viewport" content="width=device-width, initial-scale=1, interactive-widget=resizes-content">
            |    <title>$name</title>
            |    <link rel="stylesheet" href="styles.css">
            |</head>
            |<body>
            |    <div id="root" class="app-container">
            |        <div class="card">
            |            <h1>⚡ $name</h1>
            |            <p>Built with ALTRON CODE AI Mobile Studio.</p>
            |            <button id="btn">Run Action</button>
            |        </div>
            |    </div>
            |    <script src="app.js"></script>
            |</body>
            |</html>
          """.trimMargin()
        )
        File(projectDir, "styles.css").writeText(
          """html, body, #root {
            |    height: 100%;
            |    margin: 0;
            |}
            |body {
            |    background: #111317;
            |    color: #e6ebf5;
            |    font-family: system-ui, -apple-system, sans-serif;
            |    display: flex;
            |    flex-direction: column;
            |    height: 100dvh;
            |    overflow: hidden;
            |}
            |.app-container {
            |    display: flex;
            |    flex-direction: column;
            |    flex: 1;
            |    justify-content: center;
            |    align-items: center;
            |    padding: 16px;
            |}
            |.card {
            |    background: #1a1e27;
            |    border: 1px solid #ff6b00;
            |    padding: 28px;
            |    border-radius: 12px;
            |    text-align: center;
            |    box-shadow: 0 4px 20px rgba(0,0,0,0.4);
            |}
            |button {
            |    background: #ff6b00;
            |    color: #fff;
            |    border: none;
            |    padding: 12px 24px;
            |    border-radius: 8px;
            |    cursor: pointer;
            |    font-weight: bold;
            |    font-size: 14px;
            |    transition: opacity 0.2s;
            |}
            |button:active {
            |    opacity: 0.8;
            |}
          """.trimMargin()
        )
        File(projectDir, "app.js").writeText(
          """// Auto-handle visualViewport for mobile keyboard fit
            |if (window.visualViewport) {
            |    window.visualViewport.addEventListener('resize', () => {
            |        document.body.style.height = `${'$'}{window.visualViewport.height}px`;
            |    });
            |}
            |
            |document.getElementById('btn').addEventListener('click', () => {
            |    alert('ALTRON Action Executed Successfully!');
            |});
          """.trimMargin()
        )
        File(projectDir, "README.md").writeText("# $name\n\nMobile web project ready for local preview.")
      }

      ProjectType.PYTHON -> {
        File(projectDir, "main.py").writeText(
          """import sys
            |
            |def main():
            |    print("⚡ Welcome to $name running inside ALTRON CODE AI!")
            |    print(f"Python interpreter version: {sys.version}")
            |
            |if __name__ == "__main__":
            |    main()
          """.trimMargin()
        )
        File(projectDir, "requirements.txt").writeText("# Add python dependencies here\nrequests>=2.28.0\n")
        File(projectDir, "README.md").writeText("# $name\n\nPython utility workspace.")
      }

      ProjectType.KOTLIN -> {
        File(projectDir, "Main.kt").writeText(
          """fun main() {
            |    println("⚡ ALTRON CODE AI: Kotlin Core Script")
            |    val numbers = listOf(1, 2, 3, 4, 5)
            |    println("Computed sum: ${'$'}{numbers.sum()}")
            |}
          """.trimMargin()
        )
        File(projectDir, "README.md").writeText("# $name\n\nKotlin workspace.")
      }

      ProjectType.NODE -> {
        File(projectDir, "server.js").writeText(
          """const http = require('http');
            |const server = http.createServer((req, res) => {
            |  res.writeHead(200, {'Content-Type': 'application/json'});
            |  res.end(JSON.stringify({ message: "Hello from $name on ALTRON CODE AI" }));
            |});
            |server.listen(3000, () => console.log('Server running on port 3000'));
          """.trimMargin()
        )
        File(projectDir, "package.json").writeText(
          """{
            |  "name": "$sanitized",
            |  "version": "1.0.0",
            |  "main": "server.js"
            |}
          """.trimMargin()
        )
        File(projectDir, "README.md").writeText("# $name\n\nNode.js backend project.")
      }

      ProjectType.CUSTOM -> {
        File(projectDir, "README.md").writeText("# $name\n\nCustom coding workspace.")
      }
      else -> {
        File(projectDir, "README.md").writeText("# $name\n\nCoding workspace.")
      }
    }

    return Project(
      id = projectDir.name,
      name = name,
      description = description,
      type = type,
      rootDir = projectDir,
      createdAt = System.currentTimeMillis(),
      lastModified = System.currentTimeMillis()
    )
  }

  fun deleteProject(project: Project): Boolean {
    // Boundary check
    if (!isSafeWithinWorkspace(project.rootDir)) return false
    return project.rootDir.deleteRecursively()
  }

  fun listFiles(project: Project, subDirectory: File? = null): List<ProjectFile> {
    val targetDir = subDirectory ?: project.rootDir
    if (!isSafeWithinProject(project, targetDir)) return emptyList()

    val rawFiles = targetDir.listFiles { file -> !file.name.startsWith(".altron_meta") } ?: emptyArray()

    return rawFiles.map { file ->
      ProjectFile(
        file = file,
        name = file.name,
        relativePath = file.relativeTo(project.rootDir).path,
        isDirectory = file.isDirectory,
        sizeBytes = if (file.isDirectory) 0L else file.length(),
        extension = file.extension
      )
    }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
  }

  fun readFile(project: Project, file: File): String {
    if (!isSafeWithinProject(project, file)) {
      throw SecurityException("Security violation: Attempted access outside project boundary")
    }
    return if (file.exists() && file.isFile) file.readText() else ""
  }

  fun saveFile(project: Project, file: File, content: String): Boolean {
    if (!isSafeWithinProject(project, file)) {
      throw SecurityException("Security violation: Attempted write outside project boundary")
    }
    return try {
      file.parentFile?.mkdirs()
      file.writeText(content)
      true
    } catch (e: Exception) {
      false
    }
  }

  fun createFile(project: Project, parentDir: File, name: String, initialContent: String = ""): File {
    val safeTarget = File(parentDir, name)
    if (!isSafeWithinProject(project, safeTarget)) {
      throw SecurityException("Cannot create file outside project directory")
    }
    safeTarget.parentFile?.mkdirs()
    if (!safeTarget.exists()) {
      safeTarget.writeText(initialContent)
    }
    return safeTarget
  }

  fun createDirectory(project: Project, parentDir: File, name: String): File {
    val safeTarget = File(parentDir, name)
    if (!isSafeWithinProject(project, safeTarget)) {
      throw SecurityException("Cannot create directory outside project boundary")
    }
    safeTarget.mkdirs()
    return safeTarget
  }

  fun deleteFile(project: Project, file: File): Boolean {
    if (!isSafeWithinProject(project, file)) {
      throw SecurityException("Cannot delete outside project boundary")
    }
    return if (file.isDirectory) file.deleteRecursively() else file.delete()
  }

  fun renameFile(project: Project, file: File, newName: String): File {
    if (!isSafeWithinProject(project, file)) {
      throw SecurityException("Cannot rename file outside project boundary")
    }
    val target = File(file.parentFile, newName)
    if (!isSafeWithinProject(project, target)) {
      throw SecurityException("Cannot move file outside project boundary")
    }
    file.renameTo(target)
    return target
  }

  fun zipProject(project: Project): File {
    val zipName = "${project.name.replace(" ", "-")}.zip"
    val outputZip = File(context.cacheDir, zipName)
    if (outputZip.exists()) outputZip.delete()

    java.util.zip.ZipOutputStream(java.io.FileOutputStream(outputZip)).use { zipOut ->
      fun addDirToZip(folder: File, basePath: String) {
        val files = folder.listFiles() ?: return
        for (f in files) {
          if (f.name == ".altron_meta") continue
          val entryPath = if (basePath.isEmpty()) f.name else "$basePath/${f.name}"
          if (f.isDirectory) {
            val zipEntry = java.util.zip.ZipEntry("$entryPath/")
            zipOut.putNextEntry(zipEntry)
            zipOut.closeEntry()
            addDirToZip(f, entryPath)
          } else {
            val zipEntry = java.util.zip.ZipEntry(entryPath)
            zipOut.putNextEntry(zipEntry)
            f.inputStream().use { input ->
              input.copyTo(zipOut)
            }
            zipOut.closeEntry()
          }
        }
      }
      addDirToZip(project.rootDir, "")
    }
    return outputZip
  }

  // Security checks: Enforce project directory boundaries
  fun isSafeWithinProject(project: Project, file: File): Boolean {
    val canonicalRoot = project.rootDir.canonicalFile
    val canonicalFile = file.canonicalFile
    return canonicalFile.path.startsWith(canonicalRoot.path)
  }

  private fun isSafeWithinWorkspace(dir: File): Boolean {
    val canonicalWs = workspaceDir.canonicalFile
    val canonicalDir = dir.canonicalFile
    return canonicalDir.path.startsWith(canonicalWs.path) && canonicalDir != canonicalWs
  }

  private fun detectProjectType(dir: File): ProjectType {
    return when {
      File(dir, "build.gradle.kts").exists() || File(dir, "AndroidManifest.xml").exists() -> ProjectType.ANDROID
      File(dir, "index.html").exists() -> ProjectType.WEB
      File(dir, "main.py").exists() -> ProjectType.PYTHON
      File(dir, "Main.kt").exists() -> ProjectType.KOTLIN
      else -> ProjectType.CUSTOM
    }
  }

  private fun ensureDefaultProjects() {
    val existing = workspaceDir.listFiles { file -> file.isDirectory } ?: emptyArray()
    if (existing.isEmpty()) {
      createProject(
        name = "Altron-QuickStart",
        type = ProjectType.ANDROID,
        description = "Initial Android Compose starter workspace with build workflow"
      )
      createProject(
        name = "Web-Preview-Demo",
        type = ProjectType.WEB,
        description = "Lightweight mobile web prototyping sandbox"
      )
    }
  }
}
