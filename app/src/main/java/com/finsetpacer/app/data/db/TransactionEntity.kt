package com.finsetpacer.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

const val DEFAULT_CATEGORY = "UPI / Discretionary"

/**
 * A single parsed debit transaction. `smsHash` is the dedup key
 * (SHA-256 of t_date|amount|merchant) — see SmsParser. The unique index is
 * what makes TransactionDao's OnConflictStrategy.IGNORE inserts actually
 * dedup; without it, every insert succeeds regardless of a matching hash.
 */
@Entity(tableName = "transactions", indices = [Index(value = ["smsHash"], unique = true)])
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val merchant: String?,
    val category: String = DEFAULT_CATEGORY,
    val tDate: String, // "YYYY-MM-DD HH:MM:SS"
    val rawSms: String?,
    val smsHash: String
)
