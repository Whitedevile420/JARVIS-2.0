package com.jarvis.assistant

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class JarvisVoiceService : Service(),
    RecognitionListener,
    TextToSpeech.OnInitListener {

    companion object {

        const val ACTION_START = "com.jarvis.assistant.action.START"
        const val ACTION_STOP = "com.jarvis.assistant.action.STOP"
        const val ACTION_COMMAND = "com.jarvis.assistant.action.COMMAND"
        const val ACTION_LISTEN_NOW = "com.jarvis.assistant.action.LISTEN_NOW"
        const val EXTRA_COMMAND = "JARVIS_COMMAND"

        private const val CHANNEL_ID = "jarvis_channel"
        private const val NOTIFICATION_ID = 501
        private const val MODE_WAKE = 0
        private const val MODE_COMMAND = 1
        private const val RESTART_DELAY = 300L

        @Volatile
        var isRunning = false
            private set
    }

    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private val handler = Handler(Looper.getMainLooper())

    private var mode = MODE_WAKE
    private var wantsListening = false
    private var waitingForActivity = false
    private var starting = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        tts = TextToSpeech(this, this)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val action = intent?.action

        if (action == ACTION_STOP) {
            stopJarvis()
            return START_NOT_STICKY
        }

        if (action == ACTION_COMMAND) {
            val command = intent?.getStringExtra(EXTRA_COMMAND)
            if (!command.isNullOrEmpty()) {
                openMainActivityWithCommand(command)
            }
            return START_STICKY
        }

        if (action == ACTION_LISTEN_NOW) {
            if (isRunning && wantsListening) {
                startCommandListening()
            }
            return START_STICKY
        }

        if (action == ACTION_START) {
            startForegroundCompat(
                buildNotification("JARVIS starting...")
            )
            startJarvis()
            return START_STICKY
        }

        return START_STICKY
    }

    private fun startForegroundCompat(notification: Notification) {
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startJarvis() {

        if (starting) {
            return
        }

        if (isRunning) {
            updateNotification("Say \"Hey JARVIS\"")
            return
        }

        starting = true
        wantsListening = true
        waitingForActivity = false
        mode = MODE_WAKE

        if (!hasMicrophonePermission()) {
            starting = false
            updateNotification("Microphone permission required")
            stopJarvis()
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            starting = false
            updateNotification("Speech recognition unavailable")
            stopJarvis()
            return
        }

        isRunning = true
        starting = false

        updateNotification("JARVIS is active. Say \"Hey JARVIS\"")

        speak("JARVIS is active.")
    }

    override fun onInit(status: Int) {

        ttsReady = status == TextToSpeech.SUCCESS

        if (ttsReady) {
            val result = tts?.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                tts?.language = Locale.US
            }
        }

        if (isRunning && wantsListening && !waitingForActivity) {
            handler.postDelayed({
                startWakeListening()
            }, 500L)
        }
    }

    private fun speak(text: String) {

        updateNotification(text)

        if (!ttsReady || tts == null) {
            startWakeListening()
            return
        }

        try {

            tts?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "jarvis_start_" + System.currentTimeMillis()
            )

            handler.postDelayed({
                if (isRunning && wantsListening && !waitingForActivity) {
                    startWakeListening()
                }
            }, 600L)

        } catch (e: Exception) {
            startWakeListening()
        }
    }

    private fun startWakeListening() {

        if (!isRunning) return
        if (!wantsListening) return
        if (waitingForActivity) return

        mode = MODE_WAKE

        updateNotification("Listening for \"Hey JARVIS\"")

        startRecognition()
    }

    private fun startCommandListening() {

        if (!isRunning) return
        if (!wantsListening) return

        waitingForActivity = false
        mode = MODE_COMMAND

        updateNotification("Listening for command...")

        startRecognition()
    }

    private fun startRecognition() {

        if (!isRunning) return
        if (!wantsListening) return
        if (waitingForActivity) return

        if (!hasMicrophonePermission()) {
            stopJarvis()
            return
        }

        ensureRecognizer()

        val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        recognitionIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        recognitionIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        try {
            recognizer?.cancel()
        } catch (e: Exception) {
        }

        handler.postDelayed({

            if (isRunning && wantsListening && !waitingForActivity) {
                try {
                    recognizer?.startListening(recognitionIntent)
                } catch (e: Exception) {
                    recreateRecognizerAndRetry()
                }
            }

        }, 50L)
    }

    private fun ensureRecognizer() {

        if (recognizer != null) return

        try {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this)
            recognizer?.setRecognitionListener(this)
        } catch (e: Exception) {
            recognizer = null
        }
    }

    private fun recreateRecognizerAndRetry() {

        try {
            recognizer?.cancel()
        } catch (e: Exception) {
        }

        try {
            recognizer?.destroy()
        } catch (e: Exception) {
        }

        recognizer = null

        handler.postDelayed({

            if (isRunning && wantsListening && !waitingForActivity) {
                if (mode == MODE_COMMAND) {
                    startCommandListening()
                } else {
                    startWakeListening()
                }
            }

        }, RESTART_DELAY)
    }

    override fun onReadyForSpeech(params: Bundle?) {
        updateNotification(
            if (mode == MODE_COMMAND) "Listening for command..."
            else "Listening for \"Hey JARVIS\""
        )
    }

    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        updateNotification("Processing voice...")
    }

    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onError(error: Int) {

        if (!isRunning) return
        if (!wantsListening) return
        if (waitingForActivity) return

        when (error) {

            SpeechRecognizer.ERROR_NO_MATCH,
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {

                handler.postDelayed({
                    if (isRunning && wantsListening && !waitingForActivity) {
                        if (mode == MODE_COMMAND) {
                            startCommandListening()
                        } else {
                            startWakeListening()
                        }
                    }
                }, RESTART_DELAY)
            }

            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                updateNotification("Microphone permission missing")
                stopJarvis()
            }

            else -> {
                recreateRecognizerAndRetry()
            }
        }
    }

    override fun onResults(results: Bundle?) {

        if (!isRunning) return
        if (!wantsListening) return
        if (waitingForActivity) return

        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val spoken = matches?.firstOrNull()?.trim()

        if (spoken.isNullOrEmpty()) {
            restartCurrentMode()
            return
        }

        if (mode == MODE_WAKE) {

            if (containsWakeWord(spoken)) {
                startCommandListening()
            } else {
                restartCurrentMode()
            }

            return
        }

        waitingForActivity = true

        updateNotification("Command: $spoken")

        openMainActivityWithCommand(spoken)
    }

    private fun restartCurrentMode() {

        handler.postDelayed({

            if (isRunning && wantsListening && !waitingForActivity) {
                if (mode == MODE_COMMAND) {
                    startCommandListening()
                } else {
                    startWakeListening()
                }
            }

        }, RESTART_DELAY)
    }

    private fun openMainActivityWithCommand(command: String) {

        if (command.isEmpty()) {
            waitingForActivity = false
            restartCurrentMode()
            return
        }

        val intent = Intent(this, MainActivity::class.java)

        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        )

        intent.putExtra(EXTRA_COMMAND, command)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            waitingForActivity = false
            restartCurrentMode()
        }

        mode = MODE_WAKE

        handler.postDelayed({

            if (isRunning && wantsListening) {
                waitingForActivity = false
                startWakeListening()
            }

        }, 300L)
    }

    private fun containsWakeWord(text: String): Boolean {

        return text.contains("hey jarvis", ignoreCase = true) ||
                text.contains("hi jarvis", ignoreCase = true) ||
                text.contains("hello jarvis", ignoreCase = true) ||
                text.contains("jarvis", ignoreCase = true) ||
                text.contains("हे जार्विस") ||
                text.contains("जार्विस")
    }

    private fun hasMicrophonePermission(): Boolean {
        return checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun stopJarvis() {

        wantsListening = false
        isRunning = false
        starting = false
        waitingForActivity = false

        handler.removeCallbacksAndMessages(null)

        try {
            recognizer?.stopListening()
        } catch (e: Exception) {
        }

        try {
            recognizer?.cancel()
        } catch (e: Exception) {
        }

        try {
            recognizer?.destroy()
        } catch (e: Exception) {
        }

        recognizer = null

        try {
            tts?.stop()
        } catch (e: Exception) {
        }

        updateNotification("JARVIS stopped")

        stopForegroundCompat()

        stopSelf()
    }

    private fun stopForegroundCompat() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    override fun onDestroy() {

        wantsListening = false
        isRunning = false
        starting = false
        waitingForActivity = false

        handler.removeCallbacksAndMessages(null)

        try {
            recognizer?.cancel()
        } catch (e: Exception) {
        }

        try {
            recognizer?.destroy()
        } catch (e: Exception) {
        }

        recognizer = null

        try {
            tts?.stop()
        } catch (e: Exception) {
        }

        try {
            tts?.shutdown()
        } catch (e: Exception) {
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val manager = getSystemService(NotificationManager::class.java)

            if (manager?.getNotificationChannel(CHANNEL_ID) == null) {

                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "JARVIS Voice Service",
                    NotificationManager.IMPORTANCE_LOW
                )

                channel.description = "JARVIS background voice assistant."

                manager?.createNotificationChannel(channel)
            }
        }
    }

    private fun buildNotification(text: String): Notification {

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        builder.setContentTitle("JARVIS")
        builder.setContentText(text)
        builder.setSmallIcon(android.R.drawable.ic_btn_speak_now)
        builder.setOngoing(true)

        return builder.build()
    }

    private fun updateNotification(text: String) {

        val manager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager

        manager?.notify(
            NOTIFICATION_ID,
            buildNotification(text)
        )
    }
}