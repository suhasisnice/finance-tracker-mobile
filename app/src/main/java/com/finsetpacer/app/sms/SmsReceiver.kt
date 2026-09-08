package com.finsetpacer.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.finsetpacer.app.data.db.TransactionEntity
import com.finsetpacer.app.data.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Listens for android.provider.Telephony.SMS_RECEIVED, parses debit messages
 * via SmsParser, and inserts them (dedup'd by smsHash) via TransactionRepository.
 * Real-time counterpart to SmsInboxScanner's one-time backfill.
 */
@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val sender = messages[0].originatingAddress
        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }

        val parsed = SmsParser.parse(sender = sender, body = body) ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                transactionRepository.insertIfNew(
                    TransactionEntity(
                        amount = parsed.amount,
                        merchant = parsed.merchant,
                        category = parsed.category,
                        tDate = parsed.tDate,
                        rawSms = parsed.rawSms,
                        smsHash = parsed.smsHash
                    )
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
