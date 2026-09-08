package com.finsetpacer.app.domain.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * First-of-month (inclusive) / first-of-next-month (exclusive) boundary pair,
 * stored as "YYYY-MM-DD 00:00:00" text so it string-compares correctly against
 * TransactionEntity.tDate. Mirrors the [start, start+1month) window used by the
 * existing FastAPI ledger's monthly_report().
 */
data class MonthWindow(val startInclusive: String, val endExclusive: String) {
    companion object {
        val DATE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        fun forDate(date: LocalDate): MonthWindow {
            val start = date.withDayOfMonth(1)
            val end = start.plusMonths(1)
            return MonthWindow(
                startInclusive = start.atStartOfDay().format(DATE_TIME_FORMAT),
                endExclusive = end.atStartOfDay().format(DATE_TIME_FORMAT)
            )
        }

        fun current(): MonthWindow = forDate(LocalDate.now())

        /** Start-of-today, in the same text format as tDate/trackingStartDate — used to stamp a new tracking start. */
        fun todayStart(): String = LocalDate.now().atStartOfDay().format(DATE_TIME_FORMAT)
    }
}
