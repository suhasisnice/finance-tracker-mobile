package com.finsetpacer.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    /**
     * Month-window query — mirrors the boundary logic used by the existing
     * FastAPI ledger's monthly_report() (start = first-of-month, end = start + 1 month,
     * t_date >= start AND t_date < end), reimplemented against our text-column dates.
     */
    @Query(
        """
        SELECT * FROM transactions
        WHERE tDate >= :monthStart AND tDate < :monthEnd
        ORDER BY tDate DESC
        """
    )
    fun observeForMonth(monthStart: String, monthEnd: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY tDate DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM transactions WHERE smsHash = :hash")
    suspend fun existsByHash(hash: String): Int
}
