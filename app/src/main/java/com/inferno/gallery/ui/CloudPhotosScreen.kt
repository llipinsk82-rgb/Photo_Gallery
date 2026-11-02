package com.inferno.gallery.ui

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ext.SdkExtensions
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.photopicker.compose.EmbeddedPhotoPicker
import androidx.photopicker.compose.EmbeddedPhotoPickerState
import androidx.photopicker.compose.ExperimentalPhotoPickerComposeApi
import androidx.photopicker.compose.rememberEmbeddedPhotoPickerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CloudPhotosScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    suspend fun importUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val result = importCloudMedia(context, uris)
        val message = when {
            result.failed == 0 -> "Imported ${result.imported} item(s)"
            result.imported == 0 -> "Import failed"
            else -> "Imported ${result.imported}; failed: ${result.failed}"
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    val fallbackPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 50)
    ) { uris ->
        scope.launch { importUris(uris) }
    }

    val supportsEmbedded =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) >= 15

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
    ) {
        if (supportsEmbedded) {
            EmbeddedCloudPicker(
                modifier = Modifier.fillMaxSize(),
                onDownload = ::importUris
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        fallbackPicker.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageAndVideo
                            )
                        )
                    }
                ) {
                    Text("Open Cloud")
                }
            }
        }
    }
}

@OptIn(ExperimentalPhotoPickerComposeApi::class)
@Composable
private fun EmbeddedCloudPicker(
    modifier: Modifier,
    onDownload: suspend (List<Uri>) -> Unit
) {
    val scope = rememberCoroutineScope()
    lateinit var pickerState: EmbeddedPhotoPickerState

    pickerState = rememberEmbeddedPhotoPickerState(
        initialExpandedValue = true,
        onSelectionComplete = {
            val selectedUris = pickerState.selectedMedia.toList()
            if (selectedUris.isNotEmpty()) {
                scope.launch {
                    onDownload(selectedUris)
                    pickerState.deselectUris(selectedUris)
                    pickerState.isExpanded = true
                }
            }
        }
    )

    EmbeddedPhotoPicker(
        state = pickerState,
        modifier = modifier
    )
}

private data class ImportResult(
    val imported: Int,
    val failed: Int
)

private suspend fun importCloudMedia(
    context: Context,
    uris: List<Uri>
): ImportResult = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    var imported = 0
    var failed = 0

    uris.distinct().forEach { sourceUri ->
        try {
            val mimeType = resolver.getType(sourceUri) ?: "image/jpeg"
            val isVideo = mimeType.startsWith("video/")
            val displayName = resolver.query(
                sourceUri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else {
                    null
                }
            } ?: "Cloud_${System.currentTimeMillis()}"

            val collection = if (isVideo) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }

            val relativePath = if (isVideo) {
                Environment.DIRECTORY_MOVIES + "/Restored"
            } else {
                Environment.DIRECTORY_PICTURES + "/Restored"
            }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val targetUri = resolver.insert(collection, values)
                ?: error("Could not create MediaStore item")

            try {
                resolver.openInputStream(sourceUri).use { input ->
                    requireNotNull(input) { "Could not open selected media" }
                    resolver.openOutputStream(targetUri, "w").use { output ->
                        requireNotNull(output) { "Could not create local media" }
                        input.copyTo(output)
                    }
                }

                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(targetUri, values, null, null)
                imported++
            } catch (error: Exception) {
                resolver.delete(targetUri, null, null)
                throw error
            }
        } catch (_: Exception) {
            failed++
        }
    }

    ImportResult(imported = imported, failed = failed)
}
