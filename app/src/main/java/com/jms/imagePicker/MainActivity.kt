package com.jms.imagePicker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.jms.imagePicker.ui.ImagePickerNavHost
import com.jms.imagePicker.ui.ImagePreviewBar
import com.jms.imagePicker.ui.scope.ImagePickerScreen
import com.jms.imagePicker.ui.scope.PreviewScreen
import com.jms.imagePicker.ui.state.rememberImagePickerNavHostState
import com.jms.imagePicker.ui.theme.ImagePickerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImagePickerTheme(
                darkTheme = false
            ) {
                // A surface container using the 'background' color from the theme

                val iconOfExpandContent = rememberVectorPainter(
                    ImageVector.vectorResource(R.drawable.expand_content)
                )

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
                        val state = rememberImagePickerNavHostState(
                            max = 30,
                            autoSelectAfterCapture = true
                        )

                        var expand by remember {
                            mutableStateOf(false)
                        }


                        ImagePickerNavHost(
                            state = state
                        ) {
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
                                                        onClick(it)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                },
                                cellContent = {
                                    Box(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Row(
                                            modifier = Modifier.align(Alignment.BottomStart)
                                        ) {
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Column {
                                                Icon(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .background(
                                                            color = Color.Black.copy(alpha = 0.5f),
                                                            shape = RoundedCornerShape(5.dp)
                                                        )
                                                        .clickable {
                                                            onNavigateToPreviewScreen(mediaContent)
                                                        },
                                                    painter = iconOfExpandContent,
                                                    contentDescription = "expand_content",
                                                    tint = Color.White
                                                )
                                                Spacer(modifier = Modifier.height(3.dp))
                                            }
                                        }
                                        if (mediaContent.selected) {
                                            Column(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                            ) {
                                                Spacer(Modifier.height(5.dp))
                                                Row {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "",
                                                        tint = Color.Blue
                                                    )
                                                    Spacer(Modifier.width(5.dp))
                                                }
                                            }
                                        }
                                    }

                                }
                            )

                            PreviewScreen {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(start = 15.dp, top = 20.dp)
                                            .clickable {
                                                onBack()
                                            },
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "back",
                                        tint = Color.White
                                    )
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                    ) {
                                        Column {
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(
                                                        color = when (mediaContent.selected) {
                                                            true -> Color.Green.copy(alpha = 0.9f)
                                                            false -> Color.DarkGray.copy(alpha = 0.5f)
                                                        },
                                                        shape = CircleShape
                                                    )
                                                    .border(
                                                        border = BorderStroke(
                                                            width = 3.dp,
                                                            color = when (mediaContent.selected) {
                                                                true -> Color.Green.copy(alpha = 0.9f)
                                                                false -> Color.LightGray
                                                            }
                                                        ),
                                                        shape = CircleShape
                                                    )
                                                    .clickable {
                                                        onToggleSelection(mediaContent)
                                                    }
                                            ) {
                                                if (mediaContent.selected) {
                                                    Text(
                                                        modifier = Modifier
                                                            .align(Alignment.Center),
                                                        text = "${mediaContent.selectedOrder + 1}",
                                                        style = TextStyle(
                                                            fontWeight = FontWeight.W500,
                                                            color = Color.Black,
                                                            fontSize = 15.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(Modifier.width(15.dp))
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}