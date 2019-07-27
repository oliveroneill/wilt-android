package com.oliveroneill.wilt.viewmodel

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class ProfileFragmentViewModelAndroidTest {
    @Test
    fun shouldConvertToViewDataWithStringFormatting() {
        val date = LocalDateTime.now(ZoneId.of("UTC")).minusDays(8)
        val topArtist = TopArtist("Death Grips", 666, date)
        val state = ProfileCardState.LoadedTopArtist(topArtist)
        val expected = ProfileCardViewData(
            artistName = "Death Grips",
            playText = "666 plays since joining Wilt",
            lastListenedText = "Last listened to 8 days ago"
        )
        TestCase.assertEquals(expected, state.toViewData(ApplicationProvider.getApplicationContext<Context>()))
    }
}
