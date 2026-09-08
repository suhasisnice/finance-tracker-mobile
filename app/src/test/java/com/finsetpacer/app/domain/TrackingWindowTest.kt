package com.finsetpacer.app.domain

import com.finsetpacer.app.domain.model.MonthWindow
import com.finsetpacer.app.domain.model.TrackingWindow
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingWindowTest {

    @Test
    fun `an old tracking start from a previous month is clamped to this month's start`() {
        val window = TrackingWindow.effectiveWindow(trackingStartDate = "2000-01-01 00:00:00")

        assertEquals(MonthWindow.current().startInclusive, window.startInclusive)
        assertEquals(MonthWindow.current().endExclusive, window.endExclusive)
    }

    @Test
    fun `a tracking start from today is used as-is`() {
        val todayStart = MonthWindow.todayStart()
        val window = TrackingWindow.effectiveWindow(trackingStartDate = todayStart)

        assertEquals(todayStart, window.startInclusive)
    }
}
