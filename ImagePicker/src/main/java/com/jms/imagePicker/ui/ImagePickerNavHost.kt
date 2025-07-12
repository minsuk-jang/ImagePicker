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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jms.imagePicker.data.LocalMediaContentsDataSource
import com.jms.imagePicker.manager.API21MediaContentManager
import com.jms.imagePicker.manager.API29MediaContentManager
import com.jms.imagePicker.manager.FileManager
import com.jms.imagePicker.ui.picker.ImagePickerScaffold
import com.jms.imagePicker.ui.preview.PreviewScaffold
import com.jms.imagePicker.ui.scope.ImagePickerCellScope
import com.jms.imagePicker.ui.scope.ImagePickerGraphScope
import com.jms.imagePicker.ui.scope.PreviewScreenScope
import com.jms.imagePicker.ui.scope.picker.ImagePickerAlbumScope
import com.jms.imagePicker.ui.scope.picker.ImagePickerPreviewTopBarScope
import com.jms.imagePicker.ui.state.ImagePickerNavHostState
import com.jms.imagePicker.ui.state.rememberImagePickerNavHostState
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ImagePickerNavHost(
    state: ImagePickerNavHostState = rememberImagePickerNavHostState(),
    content: ImagePickerGraphScope.() -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val viewModel: ImagePickerViewModel = viewModel {
        ImagePickerViewModel(
            fileManager = FileManager(context.applicationContext),
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
        startDestination = "route_image_list",
        route = "graph_image_picker",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        val graphScopeImpl = ImagePickerGraphScopeImpl(
            viewModel = viewModel,
            navController = navController,
            state = state,
            builder = this
        )

        graphScopeImpl.content()
    }
}

internal class ImagePickerGraphScopeImpl(
    private val builder: NavGraphBuilder,
    private val navController: NavController,
    private val viewModel: ImagePickerViewModel,
    private val state: ImagePickerNavHostState,
) : ImagePickerGraphScope {
    override fun ImagePickerScreen(
        albumTopBar: @Composable ImagePickerAlbumScope.() -> Unit,
        previewTopBar: @Composable ImagePickerPreviewTopBarScope.() -> Unit,
        content: @Composable ImagePickerCellScope.() -> Unit
    ) {
        builder.composable(
            route = "route_image_list"
        ) {
            ImagePickerScaffold(
                viewModel = viewModel,
                albumTopBar = albumTopBar,
                previewTopBar = previewTopBar,
                state = state,
                content = content,
                onNavigateToPreview = {
                    navController.navigate("route_preview?$it") {
                        launchSingleTop = true

                        popUpTo("route_image_list") {
                            saveState = true
                        }
                    }
                }
            )
        }
    }

    override fun PreviewScreen(
        content: @Composable PreviewScreenScope.() -> Unit
    ) {
        builder.composable(
            route = "route_preview?{index}",
            arguments = listOf(
                navArgument("index") {
                    type = NavType.IntType
                }
            )
        ) {
            val initializeFirstVisibleItemIndex = it.arguments?.getInt("index") ?: 0

            PreviewScaffold(
                viewModel = viewModel,
                state = state,
                initializeFirstVisibleItemIndex = initializeFirstVisibleItemIndex,
                onBack = { navController.popBackStack() },
                content = content
            )
        }
    }
}