package com.alvarocervantes.fittrackplus.core.design

enum class AppThemeMode(val storageValue: String, val label: String) {
    System("system", "Sistema"),
    Light("light", "Claro"),
    Dark("dark", "Oscuro");

    companion object {
        fun fromStorageValue(value: String?): AppThemeMode {
            return entries.firstOrNull { mode -> mode.storageValue == value } ?: System
        }
    }
}

fun resolveDarkTheme(mode: AppThemeMode, systemDark: Boolean): Boolean {
    return when (mode) {
        AppThemeMode.System -> systemDark
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }
}
