package com.wb.verbum.service

import com.google.firebase.firestore.FirebaseFirestore
import com.wb.verbum.entities.User
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseService {

    private val firestore = FirebaseFirestore.getInstance()
    private val userCollection = firestore.collection("users")

    suspend fun getUserData(userId: String): User {
        return suspendCoroutine { continuation ->
            userCollection.document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userData = document.toObject(User::class.java)
                        continuation.resume(userData ?: User()) // Return user data or an empty instance
                    } else {
                        continuation.resume(User()) // Return an empty instance if user not found
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    suspend fun updateUserData(userData: User) {
        userCollection.document(userData.uuid ?: "").set(userData)
            .addOnSuccessListener {
                // Success
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }
}