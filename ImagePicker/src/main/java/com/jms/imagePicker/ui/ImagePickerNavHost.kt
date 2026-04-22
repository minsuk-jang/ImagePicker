package com.jms.imagePicker.ui

import android.os.Build
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.jms.imagePicker.data.LocalMediaContentsDataSource
import com.jms.imagePicker.manager.API21MediaContentManager
import com.jms.imagePicker.manager.API29MediaContentManager
import com.jms.imagePicker.ui.scope.ImagePickerGraphBuilder
import com.jms.imagePicker.ui.state.ImagePickerNavHostState
import com.jms.imagePicker.ui.state.rememberImagePickerNavHostState
import kotlinx.coroutines.flow.collectLatest

internal object Route {
    const val GRAPH = "graph_image_picker"
    const val PICKER = "route_image_list"
    const val PREVIEW = "route_preview?{index}"

    fun preview(index: Int) = "route_preview?$index"
}

@Composable
fun ImagePickerNavHost(
    state: ImagePickerNavHostState = rememberImagePickerNavHostState(),
    builder: ImagePickerGraphBuilder.() -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val viewModel: ImagePickerViewModel = viewModel {
        ImagePickerViewModel(
            localMediaContentsDataSource = LocalMediaContentsDataSource(
                contentManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    API29MediaContentManager(context = context.applicationContext)
                } else
                    API21MediaContentManager(context = context.applicationContext)
            )
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.selectedMediaContents.collectLatest {
            state.updateMediaContents(mediaContents = it)
        }
    }

    NavHost(
        modifier = Modifier
            .fillMaxSize(),
        navController = navController,
        startDestination = Route.PICKER,
        route = Route.GRAPH,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        val ctxImpl = ImagePickerGraphContext(
            viewModel = viewModel,
            navController = navController,
            state = state,
            builder = this
        )

        ctxImpl.builder()
    }
}

internal class ImagePickerGraphContext(
    val builder: NavGraphBuilder,
    val navController: NavController,
    val viewModel: ImagePickerViewModel,
    val state: ImagePickerNavHostState,
) : ImagePickerGraphBuilder