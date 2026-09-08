package com.finsetpacer.app.domain.model

/**
 * The window "spent since tracking start" should query: starts at whichever
 * is later — the stored trackingStartDate (set once, at onboarding) or the
 * first of the current calendar month — so a tracking start from a previous
 * month doesn't bleed into this month's pacing once the month rolls over.
 * Both dates share MonthWindow's "yyyy-MM-dd HH:mm:ss" text format, so a
 * plain string comparison picks the later one correctly.
 */
object TrackingWindow {
    fun effectiveWindow(trackingStartDate: String): MonthWindow {
        val monthWindow = MonthWindow.current()
        val effectiveStart = maxOf(trackingStartDate, monthWindow.startInclusive)
        return MonthWindow(startInclusive = effectiveStart, endExclusive = monthWindow.endExclusive)
    }
}
