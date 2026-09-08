package com.finsetpacer.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

const val DEFAULT_CATEGORY = "UPI / Discretionary"

/**
 * A single parsed debit transaction. `smsHash` is the dedup key
 * (SHA-256 of t_date|amount|merchant) — see SmsParser.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val merchant: String?,
    val category: String = DEFAULT_CATEGORY,
    val tDate: String, // "YYYY-MM-DD HH:MM:SS"
    val rawSms: String?,
    val smsHash: String
)
