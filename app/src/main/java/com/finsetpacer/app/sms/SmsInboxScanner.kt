package com.finsetpacer.app.sms

import android.content.Context
import android.provider.Telephony
import com.finsetpacer.app.data.db.TransactionEntity
import com.finsetpacer.app.data.repository.TransactionRepository
import com.finsetpacer.app.domain.OutingCostEstimator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One-time backfill run right after READ_SMS is granted (spec §4 doesn't mandate
 * this, but without it the dashboard would show ₹0 spent until the next SMS
 * arrives). Scans content://sms/inbox and feeds every message through the same
 * SmsParser used by SmsReceiver, so historical and real-time transactions land
 * in the DB identically.
 */
@Singleton
class SmsInboxScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository
) {
    /** Backfills the current calendar month only — what the live pacer dashboard needs. */
    suspend fun backfillCurrentMonth(): Int = withContext(Dispatchers.IO) {
        val monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault())
        val parsed = scanSince(monthStart.toInstant().toEpochMilli())
        transactionRepository.insertAllIfNew(parsed.map { it.toEntity() })
    }

    /**
     * Scans further back (default 6 months), stores everything found (dedup'd,
     * so this is safe to call alongside backfillCurrentMonth without double
     * counting), and returns the average of leisure-category (food/entertainment)
     * transactions as a suggested §2 "Average Outing Cost" — null if none found,
     * in which case the user should enter it manually.
     */
    suspend fun estimateOutingCostFromHistory(monthsBack: Long = 6): Double? = withContext(Dispatchers.IO) {
        val since = LocalDate.now().minusMonths(monthsBack).atStartOfDay(ZoneId.systemDefault())
        val parsed = scanSince(since.toInstant().toEpochMilli())

        transactionRepository.insertAllIfNew(parsed.map { it.toEntity() })

        val leisureAmounts = parsed
            .filter { it.category in SmsParser.LEISURE_CATEGORIES }
            .map { it.amount }
        OutingCostEstimator.estimate(leisureAmounts)
    }

    private fun scanSince(epochMillis: Long): List<ParsedTransaction> {
        val projection = arrayOf(
            Telephony.Sms.Inbox.ADDRESS,
            Telephony.Sms.Inbox.BODY,
            Telephony.Sms.Inbox.DATE
        )
        val selection = "${Telephony.Sms.Inbox.DATE} >= ?"
        val selectionArgs = arrayOf(epochMillis.toString())

        val parsedTransactions = mutableListOf<ParsedTransaction>()

        context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${Telephony.Sms.Inbox.DATE} ASC"
        )?.use { cursor ->
            val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.ADDRESS)
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.BODY)
            val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.DATE)

            while (cursor.moveToNext()) {
                val sender = cursor.getString(addressIndex)
                val body = cursor.getString(bodyIndex) ?: continue
                val dateMillis = cursor.getLong(dateIndex)
                val receivedAt = Instant.ofEpochMilli(dateMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()

                val parsed = SmsParser.parse(sender, body, receivedAt) ?: continue
                parsedTransactions += parsed
            }
        }

        return parsedTransactions
    }

    private fun ParsedTransaction.toEntity() = TransactionEntity(
        amount = amount,
        merchant = merchant,
        category = category,
        tDate = tDate,
        rawSms = rawSms,
        smsHash = smsHash
    )
}
