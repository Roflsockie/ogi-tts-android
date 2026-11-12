package com.example.ogitts.ui.theme

import androidx.compose.ui.graphics.Color

// OGI TTS Custom Colors
val OgiPrimary = Color(0xFF1976D2)        // Blue
val OgiPrimaryDark = Color(0xFF1565C0)   // Darker Blue
val OgiSecondary = Color(0xFF03DAC6)     // Teal
val OgiSecondaryDark = Color(0xFF00BFA5) // Darker Teal
val OgiAccent = Color(0xFFFF6F00)        // Orange
val OgiBackground = Color(0xFFFAFAFA)    // Light Gray
val OgiSurface = Color(0xFFFFFFFF)       // White
val OgiError = Color(0xFFD32F2F)         // Red

// Dark Theme Colors - палитра снята со скрина opencode TUI (11.11.2026):
// фон #070707, панель #14161C, границы #28292E, серый текст #A8A8A8,
// оранжевый акцент #B07E5D (в форме #D8995F - читаемее на чёрном)
val OgiPrimaryDarkTheme = Color(0xFFD8995F)    // Мягкий оранжевый акцент
val OgiSecondaryDarkTheme = Color(0xFF9A9A9A)  // Серый
val OgiBackgroundDark = Color(0xFF0A0A0A)      // Почти чёрный
val OgiSurfaceDark = Color(0xFF14161C)         // Панель

// New Design - Hover States and Enhanced Colors
val OgiPrimaryDarkHover = Color(0xFFE0A772)    // Primary hover state
val OgiSecondaryDarkHover = Color(0xFFAAAAAA)  // Secondary hover state
val OgiErrorHover = Color(0xFFC62828)          // Error hover state
val OgiTextPrimary = Color(0xFFE8E8E8)         // Primary text color
val OgiTextSecondary = Color(0xFFA0A0A0)       // Secondary text color
val OgiSuccess = Color(0xFF8FA37A)             // Success color (приглушённый зелёный)

// Legacy colors for compatibility
val Purple80 = OgiPrimaryDarkTheme
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = OgiSecondaryDarkTheme

// Палитра A+B (12.11.2026): тёмная нейтральная база + карточка состояния
// фон #09090B, панели #18181B / #141417, границы #27272A / #232329,
// текст #FAFAFA, приглушённый #A1A1AA / #71717A, акцент — мягкий оранжевый TUI
val OgiAbBackground = Color(0xFF09090B)
val OgiAbPanel = Color(0xFF18181B)
val OgiAbPanelAlt = Color(0xFF141417)
val OgiAbSheet = Color(0xFF0F0F11)
val OgiAbBorder = Color(0xFF27272A)
val OgiAbBorderSoft = Color(0xFF232329)
val OgiAbText = Color(0xFFFAFAFA)
val OgiAbTextMuted = Color(0xFFA1A1AA)
val OgiAbTextFaint = Color(0xFF71717A)
val OgiAbAccent = Color(0xFFD8995F)
val OgiAbOk = Color(0xFF8FA37A)

val Purple40 = OgiPrimary
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = OgiSecondary
