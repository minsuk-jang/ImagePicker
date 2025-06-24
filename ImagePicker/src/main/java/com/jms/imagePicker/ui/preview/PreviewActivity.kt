package com.jms.imagePicker.ui.preview

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.jms.imagePicker.Constants

internal class PreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PreviewScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = {
                    if (it.originSelected != it.selected) {
                        setResult(RESULT_OK, intent.apply {
                            putExtra(Constants.KEY_SELECTED, it.selected)
                            putExtra(Constants.KEY_URI, it.uri)
                        })
                    }
                    finish()
                }
            )
        }
    }
}