package com.example.electronicazytron.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import android.util.Log

object LastLogRepository {
    private val _lastLog = MutableStateFlow<String?>(null)
    val lastLog = _lastLog.asStateFlow()

    private const val ENDPOINT = "https://x8lj6twhcd.execute-api.us-east-1.amazonaws.com/time"

    // Devuelve el valor obtenido (formateado) o null
    suspend fun fetchFromServer(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(ENDPOINT)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                try {
                    val code = conn.responseCode
                    Log.d("LastLogRepository", "HTTP response code: $code")
                    if (code == 200) {
                        val text = conn.inputStream.bufferedReader().use { it.readText() }
                        Log.d("LastLogRepository", "Response body: $text")
                        val json = JSONObject(text)
                        if (json.has("ecuador_time")) {
                            val raw = json.getString("ecuador_time")
                            try {
                                val odt = OffsetDateTime.parse(raw)
                                val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                                val formatted = odt.format(fmt)
                                Log.d("LastLogRepository", "Parsed remote time: $formatted")
                                return@withContext formatted
                            } catch (e: Exception) {
                                Log.w("LastLogRepository", "Failed to parse remote time: ${e.message}")
                                return@withContext raw
                            }
                        } else {
                            Log.w("LastLogRepository", "Response JSON missing 'ecuador_time'")
                            return@withContext null
                        }
                    } else {
                        Log.w("LastLogRepository", "Non-200 response code: $code")
                        return@withContext null
                    }
                } finally {
                    conn.disconnect()
                }
            } catch (ex: Exception) {
                Log.e("LastLogRepository", "Error fetching remote time: ${ex.message}", ex)
                return@withContext null
            }
        }
    }

    // Permite publicar un valor tomado desde la BD (por ej. al iniciar sesión)
    fun setFromDb(value: String?) {
        _lastLog.value = value
    }
}
