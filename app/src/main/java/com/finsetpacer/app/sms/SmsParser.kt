package com.finsetpacer.app.sms

import com.finsetpacer.app.data.db.DEFAULT_CATEGORY
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Result of successfully parsing a debit SMS. `smsHash` is the dedup key used
 * by TransactionDao's UNIQUE constraint (SHA-256 of tDate|amount|merchant),
 * per spec §4 "Deduplication & Storage".
 */
data class ParsedTransaction(
    val amount: Double,
    val merchant: String?,
    val category: String,
    val tDate: String,
    val rawSms: String,
    val smsHash: String
)

/**
 * Regex-based extractor for UPI/bank debit SMS, spec §4.
 * Pure Kotlin — no Android framework dependency, unit-testable on the plain JVM.
 */
object SmsParser {

    private val DEBIT_INTENT_REGEX =
        Regex("""(?i)(debited|spent|paid|sent to|vpa|transferred)""")

    private val AMOUNT_REGEX =
        Regex("""(?i)(?:rs\.?|inr|₹)\s*([\d,]+(?:\.\d{2})?)""")

    private val MERCHANT_REGEX =
        Regex("""(?i)(?:at|to|vpa)\s+([A-Za-z0-9.\-_@\s]+?)(?:\s+on|\s+ref|\s+avail|\.|,|$)""")

    /** Loose sender-ID keywords for known UPI apps / banks — a confidence signal, not a hard filter. */
    private val KNOWN_SENDER_KEYWORDS = listOf(
        "gpay", "googlepay", "phonepe", "paytm", "cred",
        "hdfc", "sbi", "sbiinb", "icici", "axis", "kotak", "pnb", "punb"
    )

    private val MERCHANT_CATEGORY_KEYWORDS: List<Pair<Regex, String>> = listOf(
        Regex("(?i)zomato|swiggy|eatsure|dominos|mcdonald") to "Food",
        Regex("(?i)uber|ola|rapido|irctc|redbus") to "Travel",
        Regex("(?i)amazon|flipkart|myntra|ajio|meesho") to "Shopping",
        Regex("(?i)netflix|hotstar|spotify|prime\\s?video") to "Entertainment"
    )

    private val TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * @param sender the SMS sender ID (unused for hard filtering — see KNOWN_SENDER_KEYWORDS).
     * @param body the raw SMS text.
     * @param receivedAt when the SMS arrived (defaults to now; overridable for tests/backfill).
     */
    fun parse(
        sender: String?,
        body: String,
        receivedAt: LocalDateTime = LocalDateTime.now()
    ): ParsedTransaction? {
        if (!DEBIT_INTENT_REGEX.containsMatchIn(body)) return null

        val amountMatch = AMOUNT_REGEX.find(body) ?: return null
        val amount = amountMatch.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null

        val merchant = MERCHANT_REGEX.find(body)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        val tDate = receivedAt.format(TIMESTAMP_FORMAT)
        val category = categoryFor(merchant)
        val hash = hashFor(tDate, amount, merchant)

        return ParsedTransaction(
            amount = amount,
            merchant = merchant,
            category = category,
            tDate = tDate,
            rawSms = body,
            smsHash = hash
        )
    }

    fun isKnownSender(sender: String?): Boolean {
        if (sender.isNullOrBlank()) return false
        val normalized = sender.lowercase()
        return KNOWN_SENDER_KEYWORDS.any { normalized.contains(it) }
    }

    private fun categoryFor(merchant: String?): String {
        if (merchant.isNullOrBlank()) return DEFAULT_CATEGORY
        for ((pattern, category) in MERCHANT_CATEGORY_KEYWORDS) {
            if (pattern.containsMatchIn(merchant)) return category
        }
        return DEFAULT_CATEGORY
    }

    private fun hashFor(tDate: String, amount: Double, merchant: String?): String {
        val raw = "$tDate|$amount|${merchant.orEmpty()}"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }
}
