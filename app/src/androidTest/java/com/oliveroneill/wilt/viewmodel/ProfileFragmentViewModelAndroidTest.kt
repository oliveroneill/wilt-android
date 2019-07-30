package com.oliveroneill.wilt.viewmodel

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oliveroneill.wilt.data.TimeRange
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class ProfileFragmentViewModelAndroidTest {
    @Test
    fun shouldConvertToViewDataForTopArtistWithStringFormatting() {
        val date = LocalDateTime.now(ZoneId.of("UTC")).minusDays(8)
        val topArtist = TopArtist(
            "Death Grips",
            666,
            date,
            "http://notarealurl.com/album_img.png"
        )
        val state = ProfileCardState.LoadedTopArtist(TimeRange.LongTerm, topArtist)
        val expected = ProfileCardViewData(
            title = "Death Grips",
            subtitleFirstLine = "666 plays since joining Wilt",
            subtitleSecondLine = "Last listened to 8 days ago",
            tagTitle = "Your favourite artist ever",
            imageUrl = "http://notarealurl.com/album_img.png"
        )
        TestCase.assertEquals(expected, state.toViewData(ApplicationProvider.getApplicationContext<Context>()))
    }

    @Test
    fun shouldConvertToViewDataForTopTrackWithStringFormatting() {
        val date = LocalDateTime.now(ZoneId.of("UTC")).minusDays(8)
        val topTrack = TopTrack(
            "On GP by Death Grips",
            10_000,
            date,
            "http://notarealurl.com/album_img.png"
        )
        val state = ProfileCardState.LoadedTopTrack(TimeRange.LongTerm, topTrack)
        val expected = ProfileCardViewData(
            title = "On GP by Death Grips",
            subtitleFirstLine = "10 seconds spent listening since joining Wilt",
            subtitleSecondLine = "Last listened to 8 days ago",
            tagTitle = "Your favourite song ever",
            imageUrl = "http://notarealurl.com/album_img.png"
        )
        TestCase.assertEquals(expected, state.toViewData(ApplicationProvider.getApplicationContext<Context>()))
    }
}
