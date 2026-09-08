package com.finsetpacer.app.domain.model

enum class SavingsMode {
    PERCENTAGE,
    TARGET_AMOUNT;

    companion object {
        fun fromStorageValue(value: String): SavingsMode =
            entries.firstOrNull { it.name == value } ?: PERCENTAGE
    }
}
