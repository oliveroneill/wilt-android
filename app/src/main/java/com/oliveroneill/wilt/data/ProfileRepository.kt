package com.oliveroneill.wilt.data

import com.oliveroneill.wilt.viewmodel.TopArtist
import com.oliveroneill.wilt.viewmodel.TopTrack

/**
 * Interface for retrieving info for the profile screen
 */
interface ProfileRepository {
    val currentUser: String?
    fun topArtist(timeRange: TimeRange, index: Int, callback: (Result<TopArtist>) -> Unit)
    fun topTrack(timeRange: TimeRange, index: Int, callback: (Result<TopTrack>) -> Unit)
}
