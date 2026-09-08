package com.finsetpacer.app.data.repository

import com.finsetpacer.app.data.db.TransactionDao
import com.finsetpacer.app.data.db.TransactionEntity
import com.finsetpacer.app.domain.model.MonthWindow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun observeCurrentMonth(): Flow<List<TransactionEntity>> {
        val window = MonthWindow.current()
        return transactionDao.observeForMonth(window.startInclusive, window.endExclusive)
    }

    fun observeAll(): Flow<List<TransactionEntity>> = transactionDao.observeAll()

    /** Returns true if the transaction was newly inserted (false = duplicate, ignored). */
    suspend fun insertIfNew(transaction: TransactionEntity): Boolean {
        return transactionDao.insert(transaction) != -1L
    }

    suspend fun insertAllIfNew(transactions: List<TransactionEntity>): Int {
        return transactionDao.insertAll(transactions).count { it != -1L }
    }
}
