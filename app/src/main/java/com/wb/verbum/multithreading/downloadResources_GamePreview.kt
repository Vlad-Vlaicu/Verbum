package com.wb.verbum.multithreading

import android.widget.TextView
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.wb.verbum.R
import com.wb.verbum.activities.adapters.HomeGamesRecycleViewAdapter
import com.wb.verbum.model.User
import com.wb.verbum.service.StorageService
import com.wb.verbum.service.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

suspend fun downloadResources_GamePreview(
    resources: List<String>,
    holder: TextView,
    storageService: StorageService,
    user: User,
    userService: UserService,
    gameUUID: String
) {
    withContext(Dispatchers.IO) {
        try {
            holder.setTag(R.id.play_download_game_preview, true)

            val storage = Firebase.storage
            val storageRef = storage.reference
            val maxDownloadSizeBytes: Long = 1024 * 1024 * 10// 10 MB max size
            for (res in resources) {
                // Download a file
                val imagesRef = storageRef.child(res)

                imagesRef.getBytes(maxDownloadSizeBytes)
                    .addOnSuccessListener { bytes ->
                        storageService.saveFileToStorage(bytes, res)
                        holder.text = "JOACA"
                    }
                    .addOnFailureListener { exception ->
                        // Handle any errors
                        if (exception is IOException) {
                            holder.setTag(R.id.play_download_game_preview, false)
                        } else {
                            holder.setTag(R.id.play_download_game_preview, false)
                        }
                    }
            }

            user.downloadedGames?.add(gameUUID)
            userService.update(user)

        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
            holder.setTag(R.id.play_download_game_preview, false)
            holder.text = "DESCARCA JOCUL"
        } finally {
            holder.isClickable = true
        }
    }
}