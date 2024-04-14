package com.wb.verbum.activities

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.room.Room
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.wb.verbum.R
import com.wb.verbum.databinding.LoginLayoutBinding
import com.wb.verbum.db.AppDatabase
import com.wb.verbum.multithreading.syncUserDataFromFirebaseToLocal
import com.wb.verbum.service.FirebaseService
import com.wb.verbum.service.UserService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity: ComponentActivity() {

    private lateinit var binding: LoginLayoutBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        Room.databaseBuilder(this.applicationContext, AppDatabase::class.java, "app-database").build()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // TODO: Attempt Sync
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setup()
    }

    private fun setup() {
        initLoginLayout()
        binding.googleConnect.setOnClickListener {
            signInGoogle()
        }
    }

    private fun initLoginLayout() {
        val loginAnimation = binding.loginLayout.background as AnimationDrawable
        loginAnimation.apply {
            setEnterFadeDuration(0)
            setExitFadeDuration(1000)
            start()
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(task)
            }
        }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUI(account)
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken , null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful){
                val intent : Intent = Intent(this , HomeActivity::class.java)
                intent.putExtra("email" , account.email)
                intent.putExtra("name" , account.displayName)
                val userService = UserService(AppDatabase.getDatabase(this).userDao())
                val firebaseService = FirebaseService()

                val user = auth.currentUser
                val userUuid = user?.uid // Accessing the UID of the signed-in user
                GlobalScope.launch {
                    // Calling the suspend function from within the coroutine
                    if (userUuid != null) {
                        syncUserDataFromFirebaseToLocal(userService, firebaseService, user)
                    }
                }

                startActivity(intent)
            }else{
                Toast.makeText(this, it.exception.toString() , Toast.LENGTH_SHORT).show()

            }
        }
    }
}