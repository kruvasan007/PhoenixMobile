package com.example.phoenixmobile.data

import android.util.Log
import com.example.phoenixmobile.data.api_models.IngestRequest
import com.example.phoenixmobile.retrofit.RetrofitClient
import com.example.phoenixmobile.ui.chat.Triplet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ChatManager {
    private val retrofit = RetrofitClient.chatInstance

    data class ChatResult(
        val explanation: String,
        val triplets: List<Triplet>
    )

    suspend fun sendMessage(message: String): Result<ChatResult> = withContext(Dispatchers.IO) {
        val userId = try {
            AuthManager.currentUser.value?.id?.toString() ?: "unknown"
        } catch (_: Exception) {
            "unknown"
        }
        Log.d("CHAT_MANAGER", "Sending message: '$message' from user: '$userId'")

        try {
            val response = retrofit.ingestQuestion(IngestRequest(question = message, user_id = userId))
            Log.d("CHAT_MANAGER", "Response status: ${response.code()}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("CHAT_MANAGER", "Response body: $body")

                if (body != null) {
                    val triplets = body.triplets?.map { dto ->
                        Triplet(dto.subject, dto.relation, dto.`object`)
                    }
                    Log.d("CHAT_MANAGER", "Parsed triplets: ${triplets?.size}")

                    val result = ChatResult(
                        explanation = body.message.toString(),
                        triplets = triplets!!
                    )

                    Result.success(result)
                } else {
                    Log.e("CHAT_MANAGER", "Response body is null")
                    Result.failure(Exception("Пустой ответ от сервера"))
                }
            } else {
                val errorMsg = "Ошибка сервера: ${response.code()}"
                Log.e("CHAT_MANAGER", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("CHAT_MANAGER", "Network error", e)
            Result.failure(e)
        }
    }

    suspend fun clearGraph(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = retrofit.clearGraph()
            Log.d("CHAT_MANAGER", "Clear graph status: ${response.code()}")

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка очистки графа: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CHAT_MANAGER", "Clear graph error", e)
            Result.failure(e)
        }
    }
}
