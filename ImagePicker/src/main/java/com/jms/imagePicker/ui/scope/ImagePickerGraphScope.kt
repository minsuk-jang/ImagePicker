package com.jms.imagePicker.ui.scope

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jms.imagePicker.model.MediaContent
import com.jms.imagePicker.ui.ImagePickerViewModel
import com.jms.imagePicker.ui.picker.ImagePickerScaffold
import com.jms.imagePicker.ui.picker.ImagePickerState
import com.jms.imagePicker.ui.preview.PreviewScaffold


@Stable
interface ImagePickerGraphScope {
    fun ImagePickerScreen(
        albumTopBar: @Composable ImagePickerAlbumScope.() -> Unit,
        previewTopBar: @Composable ImagePickerPreviewTopBarScope.() -> Unit,
        content: @Composable BoxScope.(MediaContent) -> Unit
    )

    fun PreviewScreen(
        content: @Composable PreviewScope.(MediaContent) -> Unit
    )
}


internal class ImagePickerGraphScopeImpl(
    private val builder: NavGraphBuilder,
    private val navController: NavController,
    private val viewModel: ImagePickerViewModel,
    private val state: ImagePickerState
) : ImagePickerGraphScope {
    override fun ImagePickerScreen(
        albumTopBar: @Composable ImagePickerAlbumScope.() -> Unit,
        previewTopBar: @Composable ImagePickerPreviewTopBarScope.() -> Unit,
        content: @Composable BoxScope.(MediaContent) -> Unit
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
        content: @Composable PreviewScope.(MediaContent) -> Unit
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
                onBack = {
                    navController.popBackStack()
                },
                content = content
            )
        }
    }
}