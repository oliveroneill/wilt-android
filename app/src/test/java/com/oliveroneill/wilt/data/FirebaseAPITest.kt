package com.oliveroneill.wilt.data

import com.oliveroneill.wilt.viewmodel.TopArtist
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.time.LocalDateTime


class FirebaseAPITest {
    @Test
    fun `should convert firebase top artist response correctly`() {
        val firebaseDate = FirebaseAPI.FirebaseDate("2019-06-03T22:12:02.311Z")
        val response = FirebaseAPI.FirebaseTopArtist(
            "Death Grips", 666, firebaseDate, "http://notarealurl.com/album_img.png"
        )
        val date = LocalDateTime.of(
            2019, 6, 3, 22, 12, 2, 311000000
        )
        val expected = TopArtist(
            "Death Grips",
            666,
            date,
            "http://notarealurl.com/album_img.png"
        )
        assertEquals(response.toTopArtist(), expected)
    }
}
