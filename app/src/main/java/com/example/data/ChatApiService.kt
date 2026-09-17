package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ChatApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun testConnection(
        provider: String,
        apiKey: String,
        model: String,
        baseUrl: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid $provider API key."))
        }
        try {
            sendMessage(
                provider = provider,
                apiKey = apiKey,
                model = model,
                systemPrompt = "Respond with 'Connected'",
                history = emptyList(),
                userMessage = "Ping test"
            ).map { "Success: Connected to $provider ($model)" }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(
        provider: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        history: List<ChatMessage>,
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("API key is not configured. Please enter your $provider API key in Settings.")
            )
        }

        try {
            when (provider.uppercase()) {
                "GEMINI" -> callGemini(apiKey, model, systemPrompt, history, userMessage)
                "OPENAI" -> callOpenAi(apiKey, model, systemPrompt, history, userMessage)
                "OPENROUTER" -> callOpenRouter(apiKey, model, systemPrompt, history, userMessage)
                else -> callGemini(apiKey, model, systemPrompt, history, userMessage)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun callGemini(
        apiKey: String,
        model: String,
        systemPrompt: String,
        history: List<ChatMessage>,
        userMessage: String
    ): Result<String> {
        val targetModel = if (model.isBlank()) "gemini-3.5-flash" else model
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey"

        val contentsArray = JSONArray()

        // Include recent history (up to last 6 messages)
        val recentHistory = history.takeLast(6)
        for (msg in recentHistory) {
            val contentObj = JSONObject()
            contentObj.put("role", if (msg.sender == "user") "user" else "model")
            val partsArr = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", msg.text)
            partsArr.put(partObj)
            contentObj.put("parts", partsArr)
            contentsArray.put(contentObj)
        }

        // Add current user message
        val currentObj = JSONObject()
        currentObj.put("role", "user")
        val curParts = JSONArray()
        val curPart = JSONObject()
        curPart.put("text", userMessage)
        curParts.put(curPart)
        currentObj.put("parts", curParts)
        contentsArray.put(currentObj)

        val rootJson = JSONObject()
        rootJson.put("contents", contentsArray)

        if (systemPrompt.isNotBlank()) {
            val sysObj = JSONObject()
            val sysParts = JSONArray()
            val sp = JSONObject()
            sp.put("text", systemPrompt)
            sysParts.put(sp)
            sysObj.put("parts", sysParts)
            rootJson.put("systemInstruction", sysObj)
        }

        val request = Request.Builder()
            .url(url)
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errObj = JSONObject(responseBody)
                errObj.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}: $responseBody"
            } catch (e: Exception) {
                "HTTP ${response.code}: $responseBody"
            }
            return Result.failure(Exception("Gemini API error: $errorMsg"))
        }

        val responseJson = JSONObject(responseBody)
        val candidates = responseJson.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                val text = parts.getJSONObject(0).optString("text")
                return Result.success(text)
            }
        }

        return Result.failure(Exception("Empty response from Gemini API"))
    }

    private fun callOpenAi(
        apiKey: String,
        model: String,
        systemPrompt: String,
        history: List<ChatMessage>,
        userMessage: String
    ): Result<String> {
        val targetModel = if (model.isBlank()) "gpt-4o-mini" else model
        val url = "https://api.openai.com/v1/chat/completions"

        val messagesArr = JSONArray()

        if (systemPrompt.isNotBlank()) {
            val sysObj = JSONObject()
            sysObj.put("role", "system")
            sysObj.put("content", systemPrompt)
            messagesArr.put(sysObj)
        }

        val recentHistory = history.takeLast(6)
        for (msg in recentHistory) {
            val m = JSONObject()
            m.put("role", if (msg.sender == "user") "user" else "assistant")
            m.put("content", msg.text)
            messagesArr.put(m)
        }

        val curObj = JSONObject()
        curObj.put("role", "user")
        curObj.put("content", userMessage)
        messagesArr.put(curObj)

        val rootJson = JSONObject()
        rootJson.put("model", targetModel)
        rootJson.put("messages", messagesArr)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errObj = JSONObject(responseBody)
                errObj.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}: $responseBody"
            } catch (e: Exception) {
                "HTTP ${response.code}: $responseBody"
            }
            return Result.failure(Exception("OpenAI API error: $errorMsg"))
        }

        val responseJson = JSONObject(responseBody)
        val choices = responseJson.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
            val firstChoice = choices.getJSONObject(0)
            val message = firstChoice.optJSONObject("message")
            val text = message?.optString("content")
            if (!text.isNullOrBlank()) {
                return Result.success(text)
            }
        }

        return Result.failure(Exception("Empty response from OpenAI API"))
    }

    private fun callOpenRouter(
        apiKey: String,
        model: String,
        systemPrompt: String,
        history: List<ChatMessage>,
        userMessage: String
    ): Result<String> {
        val targetModel = if (model.isBlank()) "openrouter/auto" else model
        val url = "https://openrouter.ai/api/v1/chat/completions"

        val messagesArr = JSONArray()

        if (systemPrompt.isNotBlank()) {
            val sysObj = JSONObject()
            sysObj.put("role", "system")
            sysObj.put("content", systemPrompt)
            messagesArr.put(sysObj)
        }

        val recentHistory = history.takeLast(6)
        for (msg in recentHistory) {
            val m = JSONObject()
            m.put("role", if (msg.sender == "user") "user" else "assistant")
            m.put("content", msg.text)
            messagesArr.put(m)
        }

        val curObj = JSONObject()
        curObj.put("role", "user")
        curObj.put("content", userMessage)
        messagesArr.put(curObj)

        val rootJson = JSONObject()
        rootJson.put("model", targetModel)
        rootJson.put("messages", messagesArr)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("HTTP-Referer", "https://yaseen.app")
            .addHeader("X-Title", "Yaseen YaRVerse")
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errObj = JSONObject(responseBody)
                errObj.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}: $responseBody"
            } catch (e: Exception) {
                "HTTP ${response.code}: $responseBody"
            }
            return Result.failure(Exception("OpenRouter API error: $errorMsg"))
        }

        val responseJson = JSONObject(responseBody)
        val choices = responseJson.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
            val firstChoice = choices.getJSONObject(0)
            val message = firstChoice.optJSONObject("message")
            val text = message?.optString("content")
            if (!text.isNullOrBlank()) {
                return Result.success(text)
            }
        }

        return Result.failure(Exception("Empty response from OpenRouter API"))
    }
}
