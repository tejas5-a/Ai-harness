package com.example.data.repository

import android.content.Context
import com.example.data.model.GitHubAccount
import com.example.data.model.GitHubBranch
import com.example.data.model.GitHubCommitResult
import com.example.data.model.GitHubRepo
import com.example.data.model.Project
import com.example.data.model.ProjectType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * GitHubService: Legitimate GitHub REST API client supporting secure authentication
 * (Personal Access Token / OAuth token via Android KeyStore), repository browsing,
 * workspace cloning, direct committing & pushing, and pull request generation.
 */
class GitHubService(
  private val context: Context,
  private val secureVault: EncryptedSecurityStorage = EncryptedSecurityStorage(context)
) {

  private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
  private val tokenKey = "github_personal_access_token"

  fun getSavedToken(): String {
    return secureVault.getSecureValue(tokenKey, "")
  }

  fun saveToken(token: String) {
    secureVault.saveSecureValue(tokenKey, token.trim())
  }

  fun disconnectAccount() {
    secureVault.removeSecureValue(tokenKey)
  }

  suspend fun authenticateUser(token: String): Pair<Boolean, GitHubAccount?> = withContext(Dispatchers.IO) {
    if (token.isBlank()) return@withContext false to null
    try {
      val request = Request.Builder()
        .url("https://api.github.com/user")
        .addHeader("Authorization", "Bearer ${token.trim()}")
        .addHeader("Accept", "application/vnd.github.v3+json")
        .addHeader("User-Agent", "AltronCodeAI-Android")
        .get()
        .build()

      client.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
          val body = response.body?.string().orEmpty()
          val json = JSONObject(body)
          val account = GitHubAccount(
            username = json.optString("login", ""),
            name = json.optString("name", json.optString("login", "")),
            avatarUrl = json.optString("avatar_url", ""),
            htmlUrl = json.optString("html_url", ""),
            publicRepos = json.optInt("public_repos", 0),
            totalPrivateRepos = json.optInt("total_private_repos", 0),
            token = token.trim()
          )
          saveToken(token.trim())
          true to account
        } else {
          false to null
        }
      }
    } catch (e: Exception) {
      false to null
    }
  }

  suspend fun listUserRepositories(): List<GitHubRepo> = withContext(Dispatchers.IO) {
    val token = getSavedToken()
    if (token.isBlank()) return@withContext emptyList()

    try {
      val request = Request.Builder()
        .url("https://api.github.com/user/repos?sort=updated&per_page=40")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/vnd.github.v3+json")
        .addHeader("User-Agent", "AltronCodeAI-Android")
        .get()
        .build()

      client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext emptyList()
        val body = response.body?.string().orEmpty()
        val arr = JSONArray(body)
        val list = mutableListOf<GitHubRepo>()
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          list.add(
            GitHubRepo(
              id = obj.optLong("id", 0L),
              name = obj.optString("name", ""),
              fullName = obj.optString("full_name", ""),
              description = obj.optString("description", null),
              isPrivate = obj.optBoolean("private", false),
              htmlUrl = obj.optString("html_url", ""),
              defaultBranch = obj.optString("default_branch", "main"),
              language = obj.optString("language", null),
              stargazersCount = obj.optInt("stargazers_count", 0),
              updatedAt = obj.optString("updated_at", "")
            )
          )
        }
        list
      }
    } catch (e: Exception) {
      emptyList()
    }
  }

  suspend fun searchRepositories(query: String): List<GitHubRepo> = withContext(Dispatchers.IO) {
    val token = getSavedToken()
    val clean = query.trim()
    if (clean.isEmpty()) return@withContext emptyList()

    try {
      val reqBuilder = Request.Builder()
        .url("https://api.github.com/search/repositories?q=${java.net.URLEncoder.encode(clean, "UTF-8")}&per_page=20")
        .addHeader("Accept", "application/vnd.github.v3+json")
        .addHeader("User-Agent", "AltronCodeAI-Android")

      if (token.isNotBlank()) {
        reqBuilder.addHeader("Authorization", "Bearer $token")
      }

      client.newCall(reqBuilder.build()).execute().use { response ->
        if (!response.isSuccessful) return@withContext emptyList()
        val body = response.body?.string().orEmpty()
        val json = JSONObject(body)
        val items = json.optJSONArray("items") ?: return@withContext emptyList()
        val list = mutableListOf<GitHubRepo>()
        for (i in 0 until items.length()) {
          val obj = items.getJSONObject(i)
          list.add(
            GitHubRepo(
              id = obj.optLong("id", 0L),
              name = obj.optString("name", ""),
              fullName = obj.optString("full_name", ""),
              description = obj.optString("description", null),
              isPrivate = obj.optBoolean("private", false),
              htmlUrl = obj.optString("html_url", ""),
              defaultBranch = obj.optString("default_branch", "main"),
              language = obj.optString("language", null),
              stargazersCount = obj.optInt("stargazers_count", 0),
              updatedAt = obj.optString("updated_at", "")
            )
          )
        }
        list
      }
    } catch (e: Exception) {
      emptyList()
    }
  }

  suspend fun cloneRepoToWorkspace(
    repo: GitHubRepo,
    projectRepo: ProjectRepository
  ): Pair<Boolean, Project?> = withContext(Dispatchers.IO) {
    val token = getSavedToken()
    if (token.isBlank()) return@withContext false to null

    try {
      // 1. Create a project in ALTRON workspace
      val projType = when (repo.language?.lowercase()) {
        "kotlin", "java" -> ProjectType.ANDROID
        "html", "javascript", "typescript", "css" -> ProjectType.WEB
        "python" -> ProjectType.PYTHON
        else -> ProjectType.CUSTOM
      }

      val project = projectRepo.createProject(
        name = repo.name,
        type = projType,
        description = repo.description ?: "Cloned from GitHub ${repo.fullName}"
      )

      // 2. Fetch repository file tree via Git Trees API
      val treeUrl = "https://api.github.com/repos/${repo.fullName}/git/trees/${repo.defaultBranch}?recursive=1"
      val request = Request.Builder()
        .url(treeUrl)
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/vnd.github.v3+json")
        .addHeader("User-Agent", "AltronCodeAI-Android")
        .get()
        .build()

      client.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
          val body = response.body?.string().orEmpty()
          val json = JSONObject(body)
          val tree = json.optJSONArray("tree") ?: return@withContext true to project

          // Limit clone size to first 80 files for mobile responsiveness
          val maxFiles = minOf(tree.length(), 80)
          for (i in 0 until maxFiles) {
            val item = tree.getJSONObject(i)
            val type = item.optString("type")
            val path = item.optString("path")

            if (type == "tree") {
              File(project.rootDir, path).mkdirs()
            } else if (type == "blob") {
              // Fetch blob contents
              val blobUrl = item.optString("url")
              fetchAndSaveBlob(blobUrl, File(project.rootDir, path), token)
            }
          }
        }
      }

      true to project
    } catch (e: Exception) {
      false to null
    }
  }

  private fun fetchAndSaveBlob(blobUrl: String, targetFile: File, token: String) {
    try {
      val request = Request.Builder()
        .url(blobUrl)
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/vnd.github.v3+json")
        .addHeader("User-Agent", "AltronCodeAI-Android")
        .get()
        .build()

      client.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
          val body = response.body?.string().orEmpty()
          val json = JSONObject(body)
          val content = json.optString("content", "")
          val encoding = json.optString("encoding", "base64")

          if (encoding == "base64" && content.isNotBlank()) {
            val cleanBase64 = content.replace("\n", "").replace("\r", "")
            val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.NO_WRAP)
            targetFile.parentFile?.mkdirs()
            targetFile.writeBytes(bytes)
          }
        }
      }
    } catch (e: Exception) {
      // Ignore single file fetch error
    }
  }

  suspend fun commitAndPushFile(
    repoFullName: String,
    path: String,
    content: String,
    commitMessage: String,
    branch: String = "main"
  ): GitHubCommitResult = withContext(Dispatchers.IO) {
    val token = getSavedToken()
    if (token.isBlank()) return@withContext GitHubCommitResult(false, message = "Not authenticated with GitHub.")

    try {
      // 1. Check if file already exists to obtain its SHA
      var fileSha: String? = null
      val getUrl = "https://api.github.com/repos/$repoFullName/contents/$path?ref=$branch"
      val getReq = Request.Builder()
        .url(getUrl)
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/vnd.github.v3+json")
        .addHeader("User-Agent", "AltronCodeAI-Android")
        .get()
        .build()

      client.newCall(getReq).execute().use { res ->
        if (res.isSuccessful) {
          val b = res.body?.string().orEmpty()
          fileSha = JSONObject(b).optString("sha")
        }
      }

      // 2. Put file contents (Base64 encoded)
      val putUrl = "https://api.github.com/repos/$repoFullName/contents/$path"
      val base64Content = android.util.Base64.encodeToString(content.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)

      val bodyJson = JSONObject().apply {
        put("message", commitMessage)
        put("content", base64Content)
        put("branch", branch)
        if (!fileSha.isNullOrBlank()) {
          put("sha", fileSha)
        }
      }

      val putReq = Request.Builder()
        .url(putUrl)
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/vnd.github.v3+json")
        .addHeader("User-Agent", "AltronCodeAI-Android")
        .put(bodyJson.toString().toRequestBody(jsonMediaType))
        .build()

      client.newCall(putReq).execute().use { response ->
        if (response.isSuccessful) {
          val b = response.body?.string().orEmpty()
          val j = JSONObject(b)
          val commitObj = j.optJSONObject("commit")
          val sha = commitObj?.optString("sha")
          val htmlUrl = commitObj?.optString("html_url")
          GitHubCommitResult(
            success = true,
            commitSha = sha,
            htmlUrl = htmlUrl,
            message = "Committed successfully to $branch!"
          )
        } else {
          val err = response.body?.string().orEmpty()
          GitHubCommitResult(
            success = false,
            message = "Commit failed (HTTP ${response.code}): $err"
          )
        }
      }
    } catch (e: Exception) {
      GitHubCommitResult(false, message = "Network error: ${e.localizedMessage}")
    }
  }

  suspend fun createPullRequest(
    repoFullName: String,
    title: String,
    headBranch: String,
    baseBranch: String = "main",
    body: String = "Automated PR from ALTRON CODE AI"
  ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    val token = getSavedToken()
    if (token.isBlank()) return@withContext false to "Not authenticated with GitHub."

    try {
      val url = "https://api.github.com/repos/$repoFullName/pulls"
      val payload = JSONObject().apply {
        put("title", title)
        put("head", headBranch)
        put("base", baseBranch)
        put("body", body)
      }

      val req = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/vnd.github.v3+json")
        .addHeader("User-Agent", "AltronCodeAI-Android")
        .post(payload.toString().toRequestBody(jsonMediaType))
        .build()

      client.newCall(req).execute().use { response ->
        if (response.isSuccessful) {
          val b = response.body?.string().orEmpty()
          val htmlUrl = JSONObject(b).optString("html_url", "")
          true to "Pull Request created successfully: $htmlUrl"
        } else {
          val err = response.body?.string().orEmpty()
          false to "PR failed (HTTP ${response.code}): $err"
        }
      }
    } catch (e: Exception) {
      false to "Network error: ${e.localizedMessage}"
    }
  }
}
