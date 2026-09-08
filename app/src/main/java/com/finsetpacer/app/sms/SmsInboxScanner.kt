package com.finsetpacer.app.sms

import android.content.Context
import android.provider.Telephony
import com.finsetpacer.app.data.db.TransactionEntity
import com.finsetpacer.app.data.repository.TransactionRepository
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
 * arrives). Scans content://sms/inbox for the current calendar month only and
 * feeds every message through the same SmsParser used by SmsReceiver, so
 * historical and real-time transactions land in the DB identically.
 */
@Singleton
class SmsInboxScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository
) {
    suspend fun backfillCurrentMonth(): Int = withContext(Dispatchers.IO) {
        val monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault())
        val monthStartMillis = monthStart.toInstant().toEpochMilli()

        val projection = arrayOf(
            Telephony.Sms.Inbox.ADDRESS,
            Telephony.Sms.Inbox.BODY,
            Telephony.Sms.Inbox.DATE
        )
        val selection = "${Telephony.Sms.Inbox.DATE} >= ?"
        val selectionArgs = arrayOf(monthStartMillis.toString())

        val parsedTransactions = mutableListOf<TransactionEntity>()

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
                parsedTransactions += TransactionEntity(
                    amount = parsed.amount,
                    merchant = parsed.merchant,
                    category = parsed.category,
                    tDate = parsed.tDate,
                    rawSms = parsed.rawSms,
                    smsHash = parsed.smsHash
                )
            }
        }

        transactionRepository.insertAllIfNew(parsedTransactions)
    }
}
