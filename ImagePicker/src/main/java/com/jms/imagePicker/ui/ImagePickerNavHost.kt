package com.jms.imagePicker.ui

import android.os.Build
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.jms.imagePicker.data.LocalMediaContentsDataSource
import com.jms.imagePicker.manager.API21MediaContentManager
import com.jms.imagePicker.manager.API29MediaContentManager
import com.jms.imagePicker.manager.FileManager
import com.jms.imagePicker.ui.picker.ImagePickerState
import com.jms.imagePicker.ui.picker.rememberImagePickerState
import com.jms.imagePicker.ui.scope.ImagePickerGraphScope
import com.jms.imagePicker.ui.scope.ImagePickerGraphScopeImpl

@Composable
fun ImagePickerNavHost(
    state: ImagePickerState = rememberImagePickerState(),
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