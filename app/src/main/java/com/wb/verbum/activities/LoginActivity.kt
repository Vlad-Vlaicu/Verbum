package com.wb.verbum.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.wb.verbum.multithreading.prepareNextNotification
import com.wb.verbum.multithreading.syncUserDataFromFirebaseToLocal
import com.wb.verbum.service.FirebaseService
import com.wb.verbum.service.GameService
import com.wb.verbum.service.UserService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    private lateinit var binding: LoginLayoutBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        auth = Firebase.auth
        Room.databaseBuilder(this.applicationContext, AppDatabase::class.java, "app-database")
            .build()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userService = UserService(AppDatabase.getDatabase(this).userDao())
            val firebaseService = FirebaseService()
            val gameService = GameService(AppDatabase.getDatabase(this).gameDao())

            GlobalScope.launch {
                syncUserDataFromFirebaseToLocal(
                    userService,
                    gameService,
                    firebaseService,
                    currentUser
                )
                prepareNextNotification(this@LoginActivity, userService.getAllUsers()[0])
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            // Request the permission

            var permissionsNeeded = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsNeeded = arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }

            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded,
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
        } else {
            // Permission is already granted
            setup()
        }
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
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                val intent: Intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("email", account.email)
                intent.putExtra("name", account.displayName)
                val userService = UserService(AppDatabase.getDatabase(this).userDao())
                val gameService = GameService(AppDatabase.getDatabase(this).gameDao())
                val firebaseService = FirebaseService()

                val user = auth.currentUser
                val userUuid = user?.uid // Accessing the UID of the signed-in user
                GlobalScope.launch {
                    // Calling the suspend function from within the coroutine
                    if (userUuid != null) {
                        syncUserDataFromFirebaseToLocal(
                            userService,
                            gameService,
                            firebaseService,
                            user
                        )
                    }
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_in_anim)
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                // Perform the operation that requires this permission
                // For example, start downloading the file
                setup()
            } else {
                // Permission is denied
                // You can show a message to the user indicating why the permission is needed
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}