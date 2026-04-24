package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object FitSpacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 28.dp

    // Tokens intermedios usados en pantallas de features
    val tiny: Dp = 6.dp        // espaciado fino entre items dentro de una columna
    val smMd: Dp = 10.dp       // padding interno en filas (HeroTag, SetRow, RecordRow)
    val mdLg: Dp = 14.dp       // espaciado vertical entre items de tarjeta
    val cardPadding: Dp = 22.dp // padding de contenido en hero cards (no FitTrackCard)

    val card: Dp = 18.dp
    val section: Dp = 16.dp
    val screenHorizontal: Dp = 20.dp
    val screenTop: Dp = 12.dp
    val screenBottom: Dp = 28.dp
}
