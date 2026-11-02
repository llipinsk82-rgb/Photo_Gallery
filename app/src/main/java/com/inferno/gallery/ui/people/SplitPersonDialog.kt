package com.inferno.gallery.ui.people

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.inferno.gallery.R
import com.inferno.gallery.data.db.FaceEntity
import com.inferno.gallery.ui.theme.ShapeLarge
import com.inferno.gallery.ui.theme.ShapeSmall
import com.inferno.gallery.ui.theme.SpacingTokens
import java.io.File

@Composable
fun SplitPersonDialog(
    faces: List<FaceEntity>,
    onDismissRequest: () -> Unit,
    onConfirm: (name: String, faceIds: List<Long>) -> Unit
) {
    var selectedFaceIds by remember { mutableStateOf(setOf<Long>()) }
    var name by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = ShapeLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 420.dp, max = 720.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(SpacingTokens.L),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.M)
            ) {
                Text(
                    text = "Split person",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Select the individual faces that belong to the other person. This works per detected face, not per photo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("New person name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = if (selectedFaceIds.size == faces.size && faces.isNotEmpty()) {
                        "Leave at least one face in the current person"
                    } else {
                        "${selectedFaceIds.size} selected"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selectedFaceIds.size == faces.size && faces.isNotEmpty()) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(faces, key = { it.id }) { face ->
                        val selected = face.id in selectedFaceIds
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(ShapeSmall)
                                .then(
                                    if (selected) Modifier.border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = ShapeSmall
                                    ) else Modifier
                                )
                                .clickable {
                                    selectedFaceIds = if (selected) {
                                        selectedFaceIds - face.id
                                    } else {
                                        selectedFaceIds + face.id
                                    }
                                }
                        ) {
                            val crop = face.cropCachePath?.let(::File)
                            if (crop != null && crop.exists()) {
                                AsyncImage(
                                    model = crop,
                                    contentDescription = "Detected face",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_ms_person),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                                )
                                Surface(
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_ms_check),
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirm(name.trim(), selectedFaceIds.toList()) },
                        enabled = name.isNotBlank() &&
                            selectedFaceIds.isNotEmpty() &&
                            selectedFaceIds.size < faces.size
                    ) {
                        Text("Create person")
                    }
                }
            }
        }
    }
}


@Composable
fun UndoMergeDialog(
    sourceName: String?,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Undo last merge?") },
        text = {
            Text(
                if (sourceName.isNullOrBlank()) {
                    "The most recently merged person will be restored with the exact detected faces saved before the merge."
                } else {
                    "$sourceName will be restored with the exact detected faces saved before the merge."
                }
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Undo merge")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
