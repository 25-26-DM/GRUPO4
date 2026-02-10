package com.example.electronicazytron.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import com.example.electronicazytron.utils.CloudinaryConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CameraUtils {

    fun createTempImageFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir("images")
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Simula la subida de una imagen a la nube.
     * Si no hay red (simulado), retorna la ruta local.
     */
    suspend fun uploadImageSimulated(file: File): String {
        // Simulación simple: retraso y retorno de la ruta local
        kotlinx.coroutines.delay(500)
        return "file://${file.absolutePath}"
    }

    /**
     * Realiza la subida multipart a Cloudinary usando un `upload_preset` (unsigned).
     * Requiere configurar `CloudinaryConfig.CLOUD_NAME` y `CloudinaryConfig.UPLOAD_PRESET`.
     */
    suspend fun uploadImageToCloudinary(file: File): String {
        // Construimos URL de upload
        val cloudName = CloudinaryConfig.CLOUD_NAME
        val preset = CloudinaryConfig.UPLOAD_PRESET

        if (cloudName == "YOUR_CLOUD_NAME" || preset == "YOUR_UPLOAD_PRESET") {
            throw IllegalStateException("CloudinaryConfig no está configurado. Reemplaza CLOUD_NAME y UPLOAD_PRESET.")
        }

        val url = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"

        val client = OkHttpClient()

        val mediaType = "image/jpeg".toMediaTypeOrNull()
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody(mediaType))
            .addFormDataPart("upload_preset", preset)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Ejecutar la llamada de red en Dispatchers.IO para evitar NetworkOnMainException
        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Cloudinary upload failed: ${response.code}")
                val body = response.body?.string() ?: throw Exception("Cloudinary empty response")
                val json = JSONObject(body)
                // secure_url es la URL HTTPS retornada por Cloudinary
                json.optString("secure_url", json.optString("url", ""))
            }
        }
    }
}
