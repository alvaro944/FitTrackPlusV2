package com.alvarocervantes.fittrackplus.core.design

enum class AppDesignStyle(val storageValue: String, val label: String) {
    Classic("classic", "Clásico"),
    ModernGrit("modern_grit", "Modern Grit");

    companion object {
        fun fromStorageValue(value: String?): AppDesignStyle {
            return entries.firstOrNull { style -> style.storageValue == value } ?: Classic
        }
    }
}
