package com.jms.imagePicker.ui.preview

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier

internal class PreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PreviewScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = {
                    setResult(RESULT_OK, intent.apply {
                        putExtra("selected", it.selected)
                        putExtra("uri", it.uri)
                    })
                    finish()
                }
            )
        }
    }
}