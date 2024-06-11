package com.wb.verbum.model.exercises.exercises

import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import java.time.LocalDateTime
import java.util.UUID

class RecogniseGeometricFigures : Fragment() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var user: User
    private lateinit var game: Game
    private var mp3FilePaths: MutableList<String> = mutableListOf()
    private var imageFilePaths: MutableMap<String, MutableList<String>> = hashMapOf()
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
                game = gameService.getGameByUUID("recogniseGeometricFigures")!!
                user = userService.getAllUsers()[0]

                mp3FilePaths = mutableListOf()
                newExercise = ExerciseInfo()

                for (res in game.requiredResources!!) {
                    if (res.endsWith(".mp3", ignoreCase = true) && res.contains("shape_sounds")) {
                        mp3FilePaths.add(res)
                    }

                    if (res.endsWith(".png", ignoreCase = true) && res.contains("shape_images")) {
                        val filename = res.substringAfterLast("/")
                        // Extract the shape name by removing the numeric part and the extension
                        val shape = filename.replace(Regex("\\d+\\.png$"), "")

                        // Add the path to the map under the correct shape key
                        imageFilePaths.computeIfAbsent(shape) { mutableListOf() }.add(res)
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

        Thread.sleep(200)
        playButton.startAnimation(pulseAnimation)
        return view
    }

    private fun startGame() {
        mp3FilePaths = multiplyListToSize(mp3FilePaths, NO_ROUNDS * 3)
        mp3FilePaths.shuffle()

        mp3FilePaths = mp3FilePaths.take(NO_ROUNDS).toMutableList()

        newExercise = ExerciseInfo()
        newExercise.id = UUID.randomUUID().toString()
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
        } else {
            for (frame in imagesHoldersFrames) {
                frame.visibility = View.VISIBLE
            }

            for (image in imagesHolders) {
                image.isClickable = true
            }

            backgroundView.setBackgroundColor(resources.getColor(R.color.white))
            playButton.isClickable = true

            val shapeSound = mp3FilePaths[currentTrackIndex]
            val shapeName = extractShapeName(shapeSound)
            val shapeImage = imageFilePaths.get(shapeName)
                ?.let { storageService.retrieveFile(it.random()) }

            val otherImages = getRandomImageExcludingShape(shapeName)

            imagesHolders.shuffle()

            val bitmap = BitmapFactory.decodeFile(shapeImage?.absolutePath)
            imagesHolders[0].setImageBitmap(bitmap)
            imagesHolders[0].setTag(HOLDER_TAG, true)

            for (i in 1 until imagesHolders.size) {
                // Check if the index is within the bounds of the imageList
                if (i - 1 < otherImages.size) {
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

            initializeMediaPlayer(shapeSound)
            mediaPlayer.start()
            playButton.startAnimation(pulseAnimation)

            logNewRound(shapeName)
        }
    }

    private fun endRound(response: ImageView) {
        currentTrackIndex = currentTrackIndex + 1

        for (image in imagesHoldersFrames) {
            image.visibility = View.GONE
        }

        for (image in imagesHolders) {
            image.isClickable = false
        }

        val frameLayout = response.parent as? FrameLayout
        if (frameLayout != null) {
            frameLayout.visibility = View.VISIBLE
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

    fun extractShapeName(filePath: String): String {
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

    private fun logNewRound(name: String) {
        val round = ExerciseRound()
        round.startTime = LocalDateTime.now().toString()
        round.isCompleted = false
        round.isSuccess = false
        round.name = name
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

    fun getRandomImageExcludingShape(excludeShape: String): List<File?> {
        // Flatten the map to a list of paths excluding the specified shape
        val allPathsExcludingShape = imageFilePaths
            .filterKeys { it != excludeShape }
            .values
            .flatten()

        // Return a random path from the filtered list, or null if the list is empty
        // Shuffle the list to randomize the order
        val shuffledPaths = allPathsExcludingShape.shuffled()

        // Take up to 3 unique paths from the shuffled list
        return shuffledPaths.take(3).map { storageService.retrieveFile(it) }
    }

    fun multiplyListToSize(list: MutableList<String>, minSize: Int): MutableList<String> {
        if (list.isEmpty()) return list

        val result = mutableListOf<String>()
        while (result.size < minSize) {
            result.addAll(list)
        }

        // If the result size exceeds the minSize, trim the excess elements
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
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
        userService.update(user)
    }
}