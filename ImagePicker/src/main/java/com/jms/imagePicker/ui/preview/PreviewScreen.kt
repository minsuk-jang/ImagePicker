package com.jms.imagePicker.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.jms.imagePicker.model.Gallery


@Composable
internal fun PreviewScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val viewModel: PreviewScreenViewModel = viewModel()
    val uiModel by viewModel.uiModel.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = ImageRequest.Builder(LocalContext.current)
                .crossfade(true)
                .allowHardware(true)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .data(uiModel.uri)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.None,
        )

        IconButton(
            modifier = Modifier.align(Alignment.TopStart),
            onClick = { onBack() }
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
        }

    }
}


@Composable
@Preview(showBackground = true)
private fun Preview_PreviewScreen() {

}