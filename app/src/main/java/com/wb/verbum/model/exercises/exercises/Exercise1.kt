package com.wb.verbum.model.exercises.exercises

import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.wb.verbum.R
import com.wb.verbum.activities.HomeActivity
import com.wb.verbum.db.AppDatabase
import com.wb.verbum.model.ExerciseInfo
import com.wb.verbum.model.ExerciseRound
import com.wb.verbum.model.Game
import com.wb.verbum.model.GameStatus
import com.wb.verbum.model.User
import com.wb.verbum.service.GameService
import com.wb.verbum.service.StorageService
import com.wb.verbum.service.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Thread.sleep
import java.time.LocalDateTime

class Exercise1 : Fragment() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var user: User
    private lateinit var game: Game
    private var mp3FilePaths: MutableList<String> = mutableListOf()
    private var jpgFilePaths: MutableList<String> = mutableListOf()
    private lateinit var userService: UserService
    private lateinit var gameService: GameService
    private lateinit var storageService: StorageService
    private var imagesHolders: MutableList<ImageView> = mutableListOf()
    private var imagesHoldersFrames: MutableList<FrameLayout> = mutableListOf()
    private lateinit var newExercise: ExerciseInfo
    private var currentTrackIndex = 0
    private val NO_ROUNDS = 5
    private lateinit var playButton: ImageView
    private lateinit var pulseAnimation: Animation
    private var HOLDER_TAG = R.id.playButton
    private lateinit var backgroundView: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.exercise1demo, container, false)

        userService = UserService(AppDatabase.getDatabase(view.context).userDao())
        gameService = GameService(AppDatabase.getDatabase(view.context).gameDao())
        storageService = StorageService(view.context)

        val image1: ImageView = view.findViewById(R.id.imageView1)
        val image2: ImageView = view.findViewById(R.id.imageView2)
        val image3: ImageView = view.findViewById(R.id.imageView3)
        val image4: ImageView = view.findViewById(R.id.imageView4)
        val frame1: FrameLayout = view.findViewById(R.id.image1Frame)
        val frame2: FrameLayout = view.findViewById(R.id.image2Frame)
        val frame3: FrameLayout = view.findViewById(R.id.image3Frame)
        val frame4: FrameLayout = view.findViewById(R.id.image4Frame)
        backgroundView = view.findViewById(R.id.playBackground)
        playButton = view.findViewById(R.id.playButton)

        imagesHolders.add(image1)
        imagesHolders.add(image2)
        imagesHolders.add(image3)
        imagesHolders.add(image4)

        imagesHoldersFrames.add(frame1)
        imagesHoldersFrames.add(frame2)
        imagesHoldersFrames.add(frame3)
        imagesHoldersFrames.add(frame4)

        for (image in imagesHolders) {
            image.setOnClickListener {
                endRound(image)
            }
        }

        pulseAnimation = AnimationUtils.loadAnimation(view.context, R.anim.pulse)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                game = gameService.getGameByUUID("firstDemo")!!
                user = userService.getAllUsers()[0]

                mp3FilePaths = mutableListOf()
                newExercise = ExerciseInfo()

                for (res in game.requiredResources!!) {
                    if (res.endsWith(".mp3", ignoreCase = true) && res.contains("animal_sounds")) {
                        mp3FilePaths.add(res)
                    }

                    if (res.endsWith(".jpg", ignoreCase = true) && res.contains("animal_images")) {
                        jpgFilePaths.add(res)
                    }
                }

                startGame()
                playButton.setOnClickListener {
                    // Reset and replay the media file from the beginning
                    if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                        mediaPlayer.stop()
                        mediaPlayer.reset()
                        initializeMediaPlayer(mp3FilePaths[currentTrackIndex]);
                    }
                    mediaPlayer.start()
                    playButton.startAnimation(pulseAnimation)
                }
            }
        }

        sleep(200)
        playButton.startAnimation(pulseAnimation)
        return view
    }

    private fun startGame() {
        mp3FilePaths.shuffle()

        mp3FilePaths = mp3FilePaths.take(NO_ROUNDS).toMutableList()

        newExercise = ExerciseInfo()
        newExercise.name = game.name
        newExercise.description = game.description
        newExercise.tags = game.tags
        newExercise.rounds = mutableListOf()
        newExercise.status = GameStatus.IN_PROGRESS
        newExercise.startingTime = LocalDateTime.now().toString()

        user.exerciseHistory?.add(newExercise)

        startRound()
    }

    private fun startRound() {
        if (currentTrackIndex == NO_ROUNDS) {
            newExercise.endingTime = LocalDateTime.now().toString()
            newExercise.status = GameStatus.COMPLETED
            val intent: Intent = Intent(view?.context, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_in_anim)
            logFinishExercise()
            activity?.finish()
        }

        for (frame in imagesHoldersFrames) {
            frame.visibility = VISIBLE
        }

        backgroundView.setBackgroundColor(resources.getColor(R.color.white))
        playButton.isClickable = true

        val animalSound = mp3FilePaths[currentTrackIndex]
        val animalName = extractAnimalName(animalSound)
        val animalImage = storageService.retrieveFile(jpgFilePaths.filter {
            it.contains(
                animalName,
                ignoreCase = true
            )
        }
            .random())

        val otherImages = jpgFilePaths.filter { !it.contains(animalName, ignoreCase = true) }
            .shuffled()
            .take(3)
            .map { storageService.retrieveFile(it) }

        imagesHolders.shuffle()

        val bitmap = BitmapFactory.decodeFile(animalImage?.absolutePath)
        imagesHolders[0].setImageBitmap(bitmap)
        Log.d("AICI", "AICI main IM")
        imagesHolders[0].setTag(HOLDER_TAG, true)

        for (i in 1 until imagesHolders.size) {
            // Check if the index is within the bounds of the imageList
            if (i - 1 < otherImages.size) {
                Log.d("AICI", "AICI other IM")
                val otherImage = otherImages[i - 1]
                val otherImageBitmap = BitmapFactory.decodeFile(otherImage?.absolutePath)
                try {
                    imagesHolders[i].setImageBitmap(otherImageBitmap)
                    imagesHolders[i].setTag(HOLDER_TAG, false)

                } catch (e: Exception) {
                    e.message?.let { Log.d("AICI", it) }
                }
            }
        }

        initializeMediaPlayer(animalSound)
        mediaPlayer.start()
        playButton.startAnimation(pulseAnimation)

        logNewRound()
    }

    private fun endRound(response: ImageView) {
        currentTrackIndex = currentTrackIndex + 1

        for (image in imagesHoldersFrames) {
            image.visibility = GONE
        }

        val frameLayout = response.parent as? FrameLayout
        if (frameLayout != null) {
            frameLayout.visibility = VISIBLE
        }

        playButton.clearAnimation()
        playButton.isClickable = false

        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.reset()
        }

        if (response.getTag(HOLDER_TAG) as Boolean) {
            backgroundView.setBackgroundColor(resources.getColor(R.color.green))
            initializeMediaPlayer(getString(R.string.SUCCESS_SOUND_FILE))

        } else {
            backgroundView.setBackgroundColor(resources.getColor(R.color.red))
            initializeMediaPlayer(getString(R.string.FAIL_SOUND_FILE))
        }

        mediaPlayer.start()

        logEndRound(response.getTag(HOLDER_TAG) as Boolean)
    }

    fun extractAnimalName(filePath: String): String {
        val file = File(filePath)
        val fileName = file.nameWithoutExtension
        return fileName.substringAfterLast("/")
    }

    fun initializeMediaPlayer(fileName: String) {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        if (storageService.doesFileExistsInStorage(fileName)) {
            val file = storageService.retrieveFile(fileName)
            mediaPlayer = MediaPlayer().apply {
                if (file != null) {
                    setDataSource(file.absolutePath)
                }
                prepare()
                isLooping = false
            }
        }

        mediaPlayer.setOnCompletionListener {
            playButton.clearAnimation() // Stop animation when playback finishes
            if (fileName.contains("meta_sounds")) {
                startRound()
            }
        }
    }

    private fun logFinishExercise() {
        newExercise.endingTime = LocalDateTime.now().toString()
        newExercise.status = GameStatus.COMPLETED

        userService.update(user)
    }

    private fun logNewRound() {
        val round = ExerciseRound()
        round.startTime = LocalDateTime.now().toString()
        round.isCompleted = false
        round.isSuccess = false
        newExercise.rounds?.add(round)

        userService.update(user)
    }

    private fun logEndRound(isSuccessful: Boolean) {
        val lastRound = newExercise.rounds?.last()
        if (lastRound != null) {
            lastRound.isSuccess = isSuccessful
            lastRound.isCompleted = true
            lastRound.endTime = LocalDateTime.now().toString()

            val lastIndex = (newExercise.rounds?.size ?: 0) - 1
            newExercise.rounds?.set(lastIndex, lastRound)

            userService.update(user)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AICI", "AICI DESTROY")
        // Release the media player resources when the activity is destroyed
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }

        if (newExercise.status?.equals(GameStatus.IN_PROGRESS) == true) {
            newExercise.status = GameStatus.INCOMPLETE
            newExercise.endingTime = LocalDateTime.now().toString()
        }

        if (user.exerciseHistory == null) {
            user.exerciseHistory = mutableListOf()
        }
        user.exerciseHistory!!.add(newExercise)
        userService.update(user)
    }
}