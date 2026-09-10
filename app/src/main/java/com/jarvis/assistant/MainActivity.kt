package com.jarvis.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity(),
    RecognitionListener,
    TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false
    private lateinit var statusText: TextView
    private var pendingCommand: String? = null
    private var pendingSpeech: String? = null

    companion object {
        private const val REQUEST_RECORD_AUDIO = 1001
        private const val REQUEST_MIC_FOR_SERVICE = 1002
        private const val REQUEST_CAMERA = 1101
        private const val REQUEST_READ_CONTACTS = 1102
        private const val REQUEST_WRITE_CONTACTS = 1103
        private const val REQUEST_CALL_PHONE = 1104
        private const val REQUEST_SEND_SMS = 1105
        private const val REQUEST_OVERLAY = 1201
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildUi())

        textToSpeech = TextToSpeech(this, this)

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(this)
        } else {
            statusText.text = "Speech recognition is not available."
        }

        handleServiceCommand(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleServiceCommand(intent)
    }

    private fun handleServiceCommand(intent: Intent?) {
        val command = intent?.getStringExtra(JarvisVoiceService.EXTRA_COMMAND)

        if (!command.isNullOrEmpty()) {
            statusText.text = "JARVIS heard: $command"
            processJarvisCommand(command)
            intent?.removeExtra(JarvisVoiceService.EXTRA_COMMAND)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech?.language = Locale.US
            }

            ttsReady = true

            val waitingSpeech = pendingSpeech
            pendingSpeech = null

            if (!waitingSpeech.isNullOrEmpty()) {
                speakNow(waitingSpeech)
            }
        } else {
            ttsReady = false
            statusText.text = "Text-to-Speech is not available."
        }
    }

    private fun speak(text: String) {
        statusText.text = text

        if (!ttsReady) {
            pendingSpeech = text
            return
        }

        speakNow(text)
    }

    private fun speakNow(text: String) {
        statusText.text = text
        val tts = textToSpeech

        if (tts == null || !ttsReady) {
            pendingSpeech = text
            return
        }

        try {
            tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "jarvis_reply_" + System.currentTimeMillis()
            )
        } catch (e: Exception) {
            pendingSpeech = text
        }
    }

    private fun buildUi(): View {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER_HORIZONTAL
        root.setPadding(28, 48, 28, 28)
        root.setBackgroundColor(Color.rgb(4, 9, 18))

        val hud = TextView(this)
        hud.text = "◉  J A R V I S\nLIVE HUD"
        hud.textSize = 30f
        hud.setTextColor(Color.CYAN)
        hud.setTypeface(null, Typeface.BOLD)
        hud.gravity = Gravity.CENTER
        hud.setPadding(0, 20, 0, 8)

        val pulse = TextView(this)
        pulse.text = "● SYSTEM READY   •   VOICE STANDBY   •   SECURE MODE"
        pulse.textSize = 11f
        pulse.setTextColor(Color.LTGRAY)
        pulse.gravity = Gravity.CENTER
        pulse.setPadding(0, 0, 0, 24)

        statusText = TextView(this)
        statusText.text = "JARVIS ONLINE"
        statusText.textSize = 16f
        statusText.setTextColor(Color.WHITE)
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(18, 18, 18, 18)
        val statusBg = GradientDrawable()
        statusBg.setColor(Color.rgb(12, 24, 38))
        statusBg.setStroke(1, Color.CYAN)
        statusBg.cornerRadius = 18f
        statusText.background = statusBg

        fun button(text: String, action: () -> Unit): Button {
            val b = Button(this)
            b.text = text
            b.setTextColor(Color.WHITE)
            b.setOnClickListener { action() }
            return b
        }

        val talkButton = button("🎙  TALK TO JARVIS") { onTalkPressed() }
        val startButton = button("▶  START JARVIS") { onStartPressed() }
        val stopButton = button("■  STOP JARVIS") { onStopPressed() }
        val permissionButton = button("🔐  PERMISSION CENTER") { openPermissionCenter() }
        val overlayButton = button("◉  ENABLE LIVE FLOATING HUD") { onOverlayPressed() }

        root.addView(hud, matchParams(0, 8))
        root.addView(pulse, matchParams(0, 8))
        root.addView(statusText, matchParams(0, 10))
        root.addView(talkButton, matchParams(0, 6))
        root.addView(startButton, matchParams(0, 6))
        root.addView(stopButton, matchParams(0, 6))
        root.addView(permissionButton, matchParams(0, 6))
        root.addView(overlayButton, matchParams(0, 6))
        return root
    }

    private fun matchParams(top: Int, bottom: Int): LinearLayout.LayoutParams {
        val p = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        p.setMargins(0, top, 0, bottom)
        return p
    }

    private fun openPermissionCenter() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS
        )
        val missing = permissions.filter {
            Build.VERSION.SDK_INT < 23 ||
                checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty() && Build.VERSION.SDK_INT >= 23) {
            requestPermissions(missing.toTypedArray(), 1999)
            statusText.text = "Permission Center: review and allow the requested permissions."
        } else {
            openAppDetailsSettings()
        }
    }

    private fun openAppDetailsSettings() {
        try {
            val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            i.data = Uri.parse("package:$packageName")
            startActivity(i)
        } catch (e: Exception) {
            statusText.text = "Could not open app settings."
        }
    }

    private fun onTalkPressed() {
        if (!hasRecordAudioPermission()) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO
            )
            return
        }
        startManualListening()
    }

    private fun onStartPressed() {
        if (!hasRecordAudioPermission()) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_MIC_FOR_SERVICE
            )
            return
        }
        startJarvisService()
    }

    private fun onStopPressed() {
        val intent = Intent(this, JarvisVoiceService::class.java)
        intent.action = JarvisVoiceService.ACTION_STOP
        startService(intent)

        stopService(Intent(this, OverlayBubbleService::class.java))

        statusText.text = "JARVIS stopped."
    }

    // =====================================================
    // OVERLAY BUBBLE
    // =====================================================

    private fun onOverlayPressed() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!Settings.canDrawOverlays(this)) {

                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )

                startActivityForResult(intent, REQUEST_OVERLAY)

                return
            }
        }

        startBubble()
    }

    private fun startBubble() {

        val intent = Intent(this, OverlayBubbleService::class.java)
        startService(intent)

        statusText.text = "Floating bubble enabled. Tap it anytime to talk."
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_OVERLAY) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                Settings.canDrawOverlays(this)
            ) {
                startBubble()
            } else {
                statusText.text = "Overlay permission was not granted."
            }
        }
    }

    private fun startJarvisService() {

        val intent = Intent(this, JarvisVoiceService::class.java)
        intent.action = JarvisVoiceService.ACTION_START

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        statusText.text = "JARVIS started. Say \"Hey JARVIS\"."
    }

    private fun hasRecordAudioPermission(): Boolean {
        return checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestJarvisPermission(permission: String, command: String) {
        pendingCommand = command

        when (permission) {
            Manifest.permission.CAMERA -> {
                statusText.text = "Camera permission required."
                requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
            }
            Manifest.permission.READ_CONTACTS -> {
                statusText.text = "Contacts permission required."
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_READ_CONTACTS)
            }
            Manifest.permission.WRITE_CONTACTS -> {
                statusText.text = "Contacts write permission required."
                requestPermissions(arrayOf(Manifest.permission.WRITE_CONTACTS), REQUEST_WRITE_CONTACTS)
            }
            Manifest.permission.CALL_PHONE -> {
                statusText.text = "Phone permission required."
                requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
            }
            Manifest.permission.SEND_SMS -> {
                statusText.text = "SMS permission required."
                requestPermissions(arrayOf(Manifest.permission.SEND_SMS), REQUEST_SEND_SMS)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val granted = grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED

        if (requestCode == 1999) {
            val count = grantResults.count { it == PackageManager.PERMISSION_GRANTED }
            statusText.text = "Permission Center: $count/${grantResults.size} permissions granted."
            return
        }

        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (granted) startManualListening()
            else showPermissionDenied("Microphone permission is required.")
            return
        }

        if (requestCode == REQUEST_MIC_FOR_SERVICE) {
            if (granted) startJarvisService()
            else showPermissionDenied("Microphone permission is required for JARVIS.")
            return
        }

        if (!granted) {
            pendingCommand = null
            when (requestCode) {
                REQUEST_CAMERA -> showPermissionDenied("Camera permission was not allowed.")
                REQUEST_READ_CONTACTS -> showPermissionDenied("Contacts permission was not allowed.")
                REQUEST_WRITE_CONTACTS -> showPermissionDenied("Contacts write permission was not allowed.")
                REQUEST_CALL_PHONE -> showPermissionDenied("Phone permission was not allowed.")
                REQUEST_SEND_SMS -> showPermissionDenied("SMS permission was not allowed.")
            }
            return
        }

        val command = pendingCommand
        pendingCommand = null

        if (!command.isNullOrEmpty()) {
            processJarvisCommand(command)
        }
    }

    private fun showPermissionDenied(message: String) {
        statusText.text = message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        speak(message)
    }

    private fun processJarvisCommand(command: String) {
        statusText.text = "Processing: $command"

        CommandHandler.process(
            this,
            command,
            { reply -> speak(reply) },
            { permission -> requestJarvisPermission(permission, command) }
        )
    }

    private fun startManualListening() {
        val recognizer = speechRecognizer

        if (recognizer == null) {
            statusText.text = "Speech recognition not available."
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        statusText.text = "Listening..."

        try {
            recognizer.startListening(intent)
        } catch (e: Exception) {
            statusText.text = "Could not start listening."
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        statusText.text = "Listening..."
    }

    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        statusText.text = "Processing..."
    }

    override fun onError(error: Int) {
        statusText.text = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "I did not catch that."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is missing."
            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error during recognition."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer is busy."
            else -> "Recognition error."
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val spoken = matches?.firstOrNull()

        if (spoken.isNullOrEmpty()) {
            statusText.text = "I did not understand."
            return
        }

        statusText.text = "You said: $spoken"

        if (spoken.contains("hello jarvis", ignoreCase = true) ||
            spoken.contains("hi jarvis", ignoreCase = true) ||
            spoken.contains("hey jarvis", ignoreCase = true) ||
            spoken.contains("हे जार्विस")
        ) {
            speak("Hello. I am JARVIS. How can I help you?")
        } else {
            processJarvisCommand(spoken)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onDestroy() {
        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {}

        speechRecognizer?.destroy()
        speechRecognizer = null

        try {
            textToSpeech?.stop()
        } catch (e: Exception) {}

        textToSpeech?.shutdown()
        textToSpeech = null

        super.onDestroy()
    }
}