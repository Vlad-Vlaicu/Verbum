package com.wb.verbum.multithreading

import com.google.firebase.auth.FirebaseUser
import com.wb.verbum.entities.User
import com.wb.verbum.service.FirebaseService
import com.wb.verbum.service.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun syncUserDataFromFirebaseToLocal(userService: UserService, firebaseService: FirebaseService, user: FirebaseUser?) {
    withContext(Dispatchers.IO) {
        try {
            // Get user data from Firebase
            val firebaseUserData = user?.let { firebaseService.getUserData(it.uid) }

            // Get local user data
            val localUserData = user?.let { userService.getUserByUUID(it.uid) }

            // Compare the last updateDateTime of local and Firebase user data
            val localLastUpdated = localUserData?.lastUpdated?.toLong() ?: 0
            val firebaseLastUpdated = firebaseUserData?.lastUpdated?.toLong() ?: 0

            if (localLastUpdated == firebaseLastUpdated && localLastUpdated == 0L){
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
                firebaseService.updateUserData(newUser)
                userService.insertUser(newUser)
            }

            // If the Firebase data is more recent, update the local data
            if (firebaseLastUpdated > localLastUpdated) {
                if (firebaseUserData != null) {
                    userService.update(firebaseUserData)
                }
            } else {
                // If the local data is more recent, update the Firebase data
                if (firebaseLastUpdated < localLastUpdated){
                    if (localUserData != null) {
                        firebaseService.updateUserData(localUserData)
                    }
                }
            }
        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
        }
    }
}