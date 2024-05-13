package com.wb.verbum.service

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.firestore.FirebaseFirestore
import com.wb.verbum.model.Game
import com.wb.verbum.model.User
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseService {

    private val firestore = FirebaseFirestore.getInstance()
    private val userCollection = firestore.collection("users")
    private val gameCollection = firestore.collection("games")

    suspend fun getUserData(userId: String): User {
        return suspendCoroutine { continuation ->
            userCollection.document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userData = document.toObject(User::class.java)
                        continuation.resume(
                            userData ?: User()
                        ) // Return user data or an empty instance
                    } else {
                        continuation.resume(User()) // Return an empty instance if user not found
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    fun getGamesData(): Task<List<Game>> {

        val taskCompletionSource = TaskCompletionSource<List<Game>>()

        gameCollection.get()
            .addOnSuccessListener { games ->
                val gameList = mutableListOf<Game>()
                for (game in games) {
                    val game = game.toObject(Game::class.java)
                    gameList.add(game)
                }
                taskCompletionSource.setResult(gameList)
            }
            .addOnFailureListener { exception ->
                taskCompletionSource.setException(exception)
            }
        return taskCompletionSource.task
    }

    fun insertGame(game: Game) {
        gameCollection.add(game)
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