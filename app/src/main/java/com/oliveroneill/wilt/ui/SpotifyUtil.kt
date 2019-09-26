package com.oliveroneill.wilt.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

class SpotifyUtil {
    companion object {
        /**
         * Open Spotify track or artist
         *
         * This will navigate the user away from Wilt. It will either open [externalUrl] in the browser or
         * open [spotifyUri] in Spotify, if it's installed
         */
        fun openItem(context: Context, spotifyUri: String, externalUrl: String) {
            val url = if (isSpotifyInstalled(context.packageManager)) spotifyUri else externalUrl
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            intent.putExtra(
                Intent.EXTRA_REFERRER,
                Uri.parse("android-app://" + context.getPackageName())
            )
            context.startActivity(intent)
        }

        private fun isSpotifyInstalled(packageManager: PackageManager): Boolean {
            return try {
                packageManager.getPackageInfo("com.spotify.music", 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
}