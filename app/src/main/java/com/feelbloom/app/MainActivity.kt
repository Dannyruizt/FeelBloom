package com.feelbloom.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feelbloom.app.ui.FeelBloomRoot
import com.feelbloom.app.ui.FeelBloomViewModel
import com.feelbloom.app.ui.theme.FeelBloomTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Barras transparentes con iconos oscuros: el encabezado rosa se extiende bajo la barra de estado.
        val transparent = android.graphics.Color.TRANSPARENT
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(transparent, transparent),
            navigationBarStyle = SystemBarStyle.light(transparent, transparent),
        )

        val repository = (application as FeelBloomApp).repository

        setContent {
            FeelBloomTheme {
                val viewModel: FeelBloomViewModel = viewModel(
                    factory = FeelBloomViewModel.factory(repository)
                )
                FeelBloomRoot(viewModel)
            }
        }
    }
}
