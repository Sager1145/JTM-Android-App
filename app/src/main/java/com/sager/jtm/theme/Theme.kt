package com.sager.jtm.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors =
  lightColorScheme(
    primary = RailRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDADA),
    onPrimaryContainer = Color(0xFF410006),
    secondary = RailTeal,
    background = RailCream,
    surface = RailCream,
    onSurface = RailCharcoal,
  )

private val DarkColors =
  darkColorScheme(
    primary = RailRedDark,
    primaryContainer = Color(0xFF8F101E),
    secondary = RailTealDark,
    background = Color(0xFF191113),
    surface = Color(0xFF191113),
  )

@Composable
fun JtmTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColors
      else -> LightColors
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = JtmTypography,
    content = content,
  )
}
