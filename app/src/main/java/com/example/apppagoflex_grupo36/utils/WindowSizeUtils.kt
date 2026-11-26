package com.example.apppagoflex_grupo36.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

enum class AnchoPantalla { COMPACTA, MEDIANA, EXPANDIDA }

@Composable
fun obtenerAnchoPantalla(): AnchoPantalla {
    val anchoDp = LocalConfiguration.current.screenWidthDp
    return when {
        anchoDp < 600 -> AnchoPantalla.COMPACTA
        anchoDp < 840 -> AnchoPantalla.MEDIANA
        else -> AnchoPantalla.EXPANDIDA
    }
}