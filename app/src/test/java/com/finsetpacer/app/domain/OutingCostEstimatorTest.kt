package com.finsetpacer.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OutingCostEstimatorTest {

    @Test
    fun `returns null for no leisure history`() {
        assertNull(OutingCostEstimator.estimate(emptyList()))
    }

    @Test
    fun `averages leisure transaction amounts`() {
        val estimate = OutingCostEstimator.estimate(listOf(200.0, 400.0, 600.0))
        assertEquals(400.0, estimate!!, 0.001)
    }
}
