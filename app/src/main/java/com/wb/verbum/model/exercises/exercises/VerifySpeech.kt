package com.wb.verbum.model.exercises.exercises

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.wb.verbum.R
import java.util.Locale
import java.util.Objects

class VerifySpeech : Fragment() {

    private lateinit var resultTextView: TextView
    private lateinit var startButton: Button
    private val REQUEST_CODE_SPEECH_INPUT = 1
    private lateinit var intent: Intent
    private var speechRecognizer: SpeechRecognizer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.verify_speech_layout, container, false)

        startButton = view.findViewById(R.id.startButton)
        resultTextView = view.findViewById(R.id.resultTextView)

        startButton.setOnClickListener {
            // on below line we are calling speech recognizer intent.
            intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            // on below line we are passing language model
            // and model free form in our intent
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
               // Locale.getDefault()
                "ro-RO"
            )

            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

            // on below line we are specifying a prompt
            // message as speak to text on below line.
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Vorbeste...")

            // on below line we are specifying a try catch block.
            // in this block we are calling a start activity
            // for result method and passing our result code.

            val recognitionListener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    // Speech recognition is ready to start
                }

                override fun onBeginningOfSpeech() {
                    // Speech input has started
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // The amplitude RMS value of the current voice input has changed
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    TODO("Not yet implemented")
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    // Partial recognition results are available
                    val results =
                        partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    results?.let {
                        val firstResult = it[0]
                        // Process the first recognized word or phrase
                        handleRecognitionResult(firstResult)
                        // Stop recognition after the first result
                        stopSpeechRecognition()
                    }
                }

                override fun onResults(results: Bundle?) {
                    // Final recognition results are available
                    val finalResults =
                        results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    finalResults?.let {
                        val firstResult = it[0]
                        // Process the final recognized word or phrase
                        handleRecognitionResult(firstResult)
                    }
                }

                override fun onEndOfSpeech() {
                    // Speech input has ended
                }

                override fun onError(error: Int) {
                    // Speech recognition error occurred
                    // Handle error
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Reserved for future use
                }
            }


            // Initialize SpeechRecognizer
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
            speechRecognizer?.setRecognitionListener(recognitionListener)

            // Start listening
            speechRecognizer?.startListening(intent)

            Handler().postDelayed({
                stopSpeechRecognition()
            }, 3000)


            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)

            } catch (e: Exception) {
                // on below line we are displaying error message in toast
                Toast
                    .makeText(
                        view.context, " " + e.message,
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        }


        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // in this method we are checking request
        // code with our result code.
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            // on below line we are checking if result code is ok
            if (resultCode == -1 && data != null) {

                // in that case we are extracting the
                // data from our array list
                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

                // on below line we are setting data
                // to our output text view.
                resultTextView.text = Objects.requireNonNull(res)[0]
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
    }

    private fun stopSpeechRecognition() {
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        activity?.finishActivity(REQUEST_CODE_SPEECH_INPUT)
    }

    private fun handleRecognitionResult(result: String) {
        // Handle the recognized result (e.g., display it in a TextView)
        resultTextView.text = result
    }
}