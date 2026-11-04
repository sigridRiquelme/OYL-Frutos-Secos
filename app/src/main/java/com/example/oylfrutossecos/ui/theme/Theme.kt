package com.example.oylfrutossecos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores propios de OYL: no depender del tema oscuro o colores del celular.
private val ColoresOYL = lightColorScheme(
    primary = Color(0xFF176243),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F0E5),
    onPrimaryContainer = Color(0xFF123F2E),
    secondary = Color(0xFF568064),
    onSecondary = Color.White,
    tertiary = Color(0xFFBC8C46),
    background = Color(0xFFFAFAF5),
    onBackground = Color(0xFF203529),
    surface = Color.White,
    onSurface = Color(0xFF203529),
    surfaceVariant = Color(0xFFF0F4ED),
    onSurfaceVariant = Color(0xFF607467),
    outline = Color(0xFFCEDCD0),
    error = Color(0xFFB3261E),
    onError = Color.White
)

@Composable
fun OYLFrutosSecosTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Se mantienen los parametros para no romper las llamadas existentes.
    // El branding debe verse igual con Android en modo claro u oscuro.
    MaterialTheme(
        colorScheme = ColoresOYL,
        typography = Typography,
        content = content
    )
}
