package com.jms.imagePicker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.jms.imagePicker.ui.ImagePickerNavHost
import com.jms.imagePicker.ui.ImagePreviewBar
import com.jms.imagePicker.ui.picker.rememberImagePickerState
import com.jms.imagePicker.ui.theme.GallerySelectorTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GallerySelectorTheme(
                darkTheme = false
            ) {
                // A surface container using the 'background' color from the theme

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val arrays = if (Build.VERSION_CODES.TIRAMISU <= Build.VERSION.SDK_INT) {
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                    } else
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

                    if (ContextCompat.checkSelfPermission(
                            this,
                            arrays[0]
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val state = rememberImagePickerState(
                            max = 30,
                            autoSelectAfterCapture = true
                        )

                        val images = state.mediaContents
                        Log.e("jms8732", "images: $images")

                        var expand by remember {
                            mutableStateOf(false)
                        }

                        ImagePickerNavHost {
                            ImagePickerScreen(
                                albumTopBar = {
                                    Row {
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            modifier = Modifier
                                                .height(48.dp)
                                                .clickable {
                                                    expand = true
                                                }
                                                .wrapContentHeight(Alignment.CenterVertically),
                                            text = "${selectedAlbum?.name}(${selectedAlbum?.count})",
                                            fontSize = 20.sp,
                                            color = Color.Black,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        DropdownMenu(
                                            modifier = Modifier.wrapContentSize(),
                                            expanded = expand, onDismissRequest = { }) {
                                            albums.forEach {
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(text = "${it.name}(${it.count})")
                                                    },
                                                    onClick = {
                                                        expand = false
                                                        onSelect(it)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                },
                                previewTopBar = {
                                    AnimatedVisibility(
                                        visible = selectedMediaContents.isNotEmpty(),
                                        enter = slideInVertically() + expandVertically(expandFrom = Alignment.Top)
                                                + fadeIn(initialAlpha = 0.3f),
                                        exit = slideOutVertically() + shrinkVertically() + fadeOut()
                                    ) {
                                        ImagePreviewBar(
                                            mediaContents = selectedMediaContents,
                                            onClick = { mediaContent ->
                                                onClick(mediaContent)
                                            }
                                        )
                                    }
                                },
                                content = {
                                    if (it.selected) {
                                        Box(
                                            modifier = Modifier
                                                .border(width = 3.5.dp, color = Color.Green)
                                                .background(color = Gray.copy(0.5f))
                                                .fillMaxSize()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                            ) {
                                                Column {
                                                    Spacer(Modifier.height(5.dp))
                                                    Text(
                                                        modifier = Modifier
                                                            .background(
                                                                color = Color.Green,
                                                                shape = CircleShape
                                                            )
                                                            .size(20.dp),
                                                        text = "${it.selectedOrder + 1}",
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                                Spacer(Modifier.width(5.dp))
                                            }
                                        }
                                    }
                                }
                            )

                            PreviewScreen {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = when (it.selected) {
                                                    true -> Color.White.copy(alpha = 0.9f)
                                                    false -> Color.DarkGray.copy(alpha = 0.5f)
                                                },
                                                shape = CircleShape
                                            )
                                            .border(
                                                border = BorderStroke(
                                                    width = 3.dp,
                                                    color = when (it.selected) {
                                                        true -> Color.White.copy(alpha = 0.9f)
                                                        false -> Color.LightGray
                                                    }
                                                ),
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                this@PreviewScreen.onClick(it)
                                            }
                                    ) {
                                        if (it.selected) {
                                            Text(
                                                modifier = Modifier
                                                    .align(Alignment.Center),
                                                text = "${it.selectedOrder + 1}",
                                                style = TextStyle(
                                                    fontWeight = FontWeight.W500,
                                                    color = Color.Black,
                                                    fontSize = 15.sp
                                                )
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(5.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}