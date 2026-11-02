package com.inferno.gallery.ui

import android.net.Uri
import android.os.Build
import android.os.ext.SdkExtensions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.photopicker.compose.EmbeddedPhotoPicker
import androidx.photopicker.compose.ExperimentalPhotoPickerComposeApi
import androidx.photopicker.compose.rememberEmbeddedPhotoPickerState
import coil3.compose.AsyncImage

/**
 * The system's Photo Picker shows the device and configured cloud providers.
 * BlackServ never handles Google credentials and only sees user-selected URIs.
 */
@Composable
fun CloudPhotosScreen(contentPadding: PaddingValues) {
    val selected = remember { mutableStateListOf<Uri>() }
    val fallbackPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 50)
    ) { uris ->
        selected.clear()
        selected.addAll(uris)
    }
    val supportsEmbedded = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
        SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) >= 15

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Google Photos", style = MaterialTheme.typography.titleLarge)
            Text(
                "Zdjęcia z konta zalogowanego w Google Photos. Nie podajesz hasła " +
                    "w BlackServ. Ty decydujesz, które zdjęcia udostępnisz aplikacji.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (supportsEmbedded) {
            EmbeddedCloudPicker(
                modifier = Modifier.weight(1f),
                onSelected = { uris ->
                    uris.forEach { uri ->
                        if (uri !in selected) selected.add(uri)
                    }
                },
                onDeselected = { uris -> selected.removeAll(uris.toSet()) }
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Wbudowany widok chmury nie jest dostępny na tym Androidzie. " +
                        "Otwórz systemowy wybór zdjęć, który może zawierać Google Photos.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = {
                    fallbackPicker.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageAndVideo
                        )
                    )
                }) {
                    Text("Otwórz zdjęcia Google")
                }
            }
        }
        if (selected.isNotEmpty()) {
            Text(
                "Wybrano " + selected.size + " zdjęć lub filmów",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selected, key = { it.toString() }) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Wybrany element chmury",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPhotoPickerComposeApi::class)
@Composable
private fun EmbeddedCloudPicker(
    modifier: Modifier,
    onSelected: (List<Uri>) -> Unit,
    onDeselected: (List<Uri>) -> Unit
) {
    val pickerState = rememberEmbeddedPhotoPickerState(
        initialExpandedValue = true,
        onUriPermissionGranted = onSelected,
        onUriPermissionRevoked = onDeselected,
        onSelectionComplete = {}
    )
    EmbeddedPhotoPicker(
        state = pickerState,
        modifier = modifier.fillMaxWidth()
    )
}
