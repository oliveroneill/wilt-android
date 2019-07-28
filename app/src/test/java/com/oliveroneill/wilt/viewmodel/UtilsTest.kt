package com.oliveroneill.wilt.viewmodel

import junit.framework.TestCase
import org.junit.Test

class UtilsTest {
    @Test
    fun `should convert milliseconds to duration`() {
        TestCase.assertEquals("10 seconds", 10_000L.toDurationFromMilliseconds())
        TestCase.assertEquals("1 minute", 60_000L.toDurationFromMilliseconds())
        TestCase.assertEquals("10 minutes", 600_000L.toDurationFromMilliseconds())
        TestCase.assertEquals("3 hours", (3 * 60 * 60 * 1000L).toDurationFromMilliseconds())
        TestCase.assertEquals("1 day", (24 * 60 * 60 * 1000L).toDurationFromMilliseconds())
        TestCase.assertEquals("2 weeks 2 days", (16 * 24 * 60 * 60 * 1000L).toDurationFromMilliseconds())
        TestCase.assertEquals("10 minutes 1 second", 601_000L.toDurationFromMilliseconds())
    }
}