package com.example.cameraxapp.ui.gallery

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class MediaFile(val uri: Uri, val isVideo: Boolean)

@Composable
fun GalleryScreen() {
    GalleryContent()
}

@Composable
private fun GalleryContent() {
    val context = LocalContext.current
    var mediaFiles by remember { mutableStateOf<List<MediaFile>>(emptyList()) }

    LaunchedEffect(Unit) {
        mediaFiles = loadMedia(context)
    }

    if (mediaFiles.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No photos or videos found.")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(mediaFiles) { mediaFile ->
                MediaItem(mediaFile = mediaFile)
            }
        }
    }
}

@Composable
private fun MediaItem(mediaFile: MediaFile) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    val mimeType = if (mediaFile.isVideo) "video/*" else "image/*"
                    setDataAndType(mediaFile.uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(intent)
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = mediaFile.uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (mediaFile.isVideo) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = "Play Video",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

private fun loadMedia(context: Context): List<MediaFile> {
    val mediaList = mutableListOf<MediaFile>()
    val imageProjection = arrayOf(MediaStore.Images.Media._ID)
    val videoProjection = arrayOf(MediaStore.Video.Media._ID)
    val imageSelection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
    val videoSelection = "${MediaStore.Video.Media.RELATIVE_PATH} LIKE ?"
    val selectionArgs = arrayOf("%CameraX-%")

    // Query Images
    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        imageProjection,
        imageSelection,
        selectionArgs,
        "${MediaStore.Images.Media.DATE_TAKEN} DESC"
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            mediaList.add(MediaFile(uri = contentUri, isVideo = false))
        }
    }

    // Query Videos
    context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        videoProjection,
        videoSelection,
        selectionArgs,
        "${MediaStore.Video.Media.DATE_TAKEN} DESC"
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
            mediaList.add(MediaFile(uri = contentUri, isVideo = true))
        }
    }

    return mediaList.sortedByDescending { it.uri.toString() } // A simple sort, can be improved
}
