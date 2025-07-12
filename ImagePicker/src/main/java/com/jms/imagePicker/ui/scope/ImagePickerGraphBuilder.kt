package com.jms.imagePicker.ui.scope

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jms.imagePicker.ui.ImagePickerGraphContext
import com.jms.imagePicker.ui.picker.ImagePickerScaffold
import com.jms.imagePicker.ui.preview.PreviewScaffold
import com.jms.imagePicker.ui.scope.picker.ImagePickerAlbumScope
import com.jms.imagePicker.ui.scope.picker.ImagePickerPreviewScope

@Stable
interface ImagePickerGraphBuilder

/**
 *
 * Declares an image picker screen in the navigation graph.
 *
 * @param albumTopBar: A composable slot for customizing the album selection top bar.
 * @param previewTopBar: A composable slot for showing selected media in the preview top bar.
 * @param cellContent: A composable for rendering each image cell in the grid.
 */
fun ImagePickerGraphBuilder.ImagePickerScreen(
    albumTopBar: @Composable ImagePickerAlbumScope.() -> Unit = {},
    previewTopBar: @Composable ImagePickerPreviewScope.() -> Unit = {},
    cellContent: @Composable ImagePickerCellScope.() -> Unit = {}
) {
    this as? ImagePickerGraphContext
        ?: error("ImagePickerGraphBuilder is not ImagePickerGraphContext")

    builder.composable(
        route = "route_image_list"
    ) {
        ImagePickerScaffold(
            viewModel = viewModel,
            albumTopBar = albumTopBar,
            previewTopBar = previewTopBar,
            state = state,
            cellContent = cellContent,
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

/**
 * Displays the full preview screen for selected media.
 *
 * @param content A composable for customizing the preview UI layout.
 */
fun ImagePickerGraphBuilder.PreviewScreen(
    content: @Composable PreviewScreenScope.() -> Unit = { }
){
    this as? ImagePickerGraphContext ?: error("ImagePickerGraphBuilder is not ImagePickerGraphContext")
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