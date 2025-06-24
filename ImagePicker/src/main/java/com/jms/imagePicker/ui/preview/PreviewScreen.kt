package com.jms.imagePicker.ui.preview

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.UiMode
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.jms.imagePicker.Constants
import androidx.core.net.toUri


@Composable
internal fun PreviewScreen(
    modifier: Modifier = Modifier,
    onBack: (PreviewScreenUiModel) -> Unit = {}
) {
    val viewModel: PreviewScreenViewModel = viewModel()
    val uiModel by viewModel.uiModel.collectAsStateWithLifecycle()

    BackHandler {
        onBack(uiModel)
    }

    PreviewContent(
        modifier = modifier,
        uiModel = uiModel,
        onBack = onBack,
        onSelect = viewModel::select
    )
}


@Composable
private fun PreviewContent(
    modifier: Modifier = Modifier,
    uiModel: PreviewScreenUiModel,
    onBack: (PreviewScreenUiModel) -> Unit = {},
    onSelect: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = { onBack(uiModel) }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back",
                    tint = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AsyncImage(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.Center),
                model = ImageRequest.Builder(LocalContext.current)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .data(uiModel.uri.toUri())
                    .build(),
                contentDescription = "content",
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(
                            color = when (uiModel.selected) {
                                true -> Color.LightGray
                                false -> Color.Transparent
                            },
                            shape = CircleShape
                        )
                        .border(
                            border = BorderStroke(width = 2.dp, color = Color.LightGray),
                            shape = CircleShape
                        )
                        .clickable {
                            onSelect()
                        }
                ) {
                    if (uiModel.selected)
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "${uiModel.order}",
                            style = TextStyle(
                                fontWeight = FontWeight.W500,
                                color = Color.Black
                            )
                        )
                }

                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview_PreviewContent(
    @PreviewParameter(PreviewScreenParameter::class) uiModel: PreviewScreenUiModel
) {
    PreviewContent(
        uiModel = uiModel,
        onBack = {}
    )
}


private class PreviewScreenParameter : PreviewParameterProvider<PreviewScreenUiModel> {
    override val values: Sequence<PreviewScreenUiModel>
        get() = sequenceOf(
            PreviewScreenUiModel(
                uri = "",
                selected = true,
                order = 1
            ),
            PreviewScreenUiModel(
                uri = "",
                selected = false,
                order = 1
            )
        )
}