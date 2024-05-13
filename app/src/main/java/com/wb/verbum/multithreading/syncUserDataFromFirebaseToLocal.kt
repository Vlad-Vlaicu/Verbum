package com.wb.verbum.multithreading

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.wb.verbum.model.Game
import com.wb.verbum.model.User
import com.wb.verbum.service.FirebaseService
import com.wb.verbum.service.GameService
import com.wb.verbum.service.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend fun syncUserDataFromFirebaseToLocal(
    userService: UserService,
    gameService: GameService,
    firebaseService: FirebaseService,
    user: FirebaseUser?
) {
    withContext(Dispatchers.IO) {
        val gamesToBeInserted = arrayListOf<Game>()
        try {

            // Get user data from Firebase
            val firebaseUserData = user?.let { firebaseService.getUserData(it.uid) }

            // Get local user data
            val localUserData = user?.let { userService.getUserByUUID(it.uid) }

            // Compare the last updateDateTime of local and Firebase user data
            val localLastUpdated = localUserData?.lastUpdated?.toLong() ?: 0
            val firebaseLastUpdated = firebaseUserData?.lastUpdated?.toLong() ?: 0

            if (localLastUpdated.toInt()
                    .toLong() == firebaseLastUpdated && localLastUpdated == 0L
            ) {
                val newUser = User()
                if (user != null) {
                    newUser.uuid = user.uid
                }
                if (user != null) {
                    newUser.name = user.displayName
                }
                if (user != null) {
                    newUser.email = user.email
                }
                newUser.lastUpdated = (System.currentTimeMillis() / 1000).toString()
                newUser.exerciseHistory = arrayListOf()
                newUser.downloadedGames = arrayListOf()
                newUser.favGames = arrayListOf()
                firebaseService.updateUserData(newUser)
                userService.insertUser(newUser)
            }

            // If the Firebase data is more recent, update the local data
            if (firebaseLastUpdated > localLastUpdated) {
                if (firebaseUserData != null) {
                    userService.updateByFirebase(firebaseUserData)
                }
            } else {
                // If the local data is more recent, update the Firebase data
                if (firebaseLastUpdated < localLastUpdated) {
                    if (localUserData != null) {
                        firebaseService.updateUserData(localUserData)
                    }
                }
            }

            val task = firebaseService.getGamesData()
            val dbGames = gameService.getAllGames();

            task.addOnSuccessListener { gameList ->
                // Handle success: gameList contains the list of games retrieved from Firestore
                for (game in gameList) {
                    if (game in dbGames) {
                        continue
                    } else {
                        gamesToBeInserted.add(game)
                    }
                }

                GlobalScope.launch(Dispatchers.IO) {
                    // Insert each game in gamesToBeInserted
                    gameService.insertGames(gamesToBeInserted)
                }
            }
            // Add failure listener to handle any exceptions
            task.addOnFailureListener { exception ->
                // Handle failure: exception contains the exception that occurred during the operation
                Log.e("TAG", "Error fetching games from Firestore", exception)
            }


        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
        }
    }
}