package com.example.engine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceRecognitionEngine(private val context: Context) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private val _partialResult = MutableStateFlow("")
    val partialResult: StateFlow<String> = _partialResult.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    var onCommandRecognized: ((String) -> Unit)? = null

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening() {
        if (!isAvailable()) {
            _lastError.value = "Reconhecimento de voz não disponível no dispositivo."
            return
        }

        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@VoiceRecognitionEngine)
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "pt-BR")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "pt-BR")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            }

            _partialResult.value = ""
            _lastError.value = null
            _isListening.value = true
            speechRecognizer?.startListening(intent)
            Log.d("VoiceRecognitionEngine", "Iniciando escuta de voz contínua...")
        } catch (e: Exception) {
            _isListening.value = false
            _lastError.value = e.localizedMessage ?: "Erro ao inicializar microfone"
            Log.e("VoiceRecognitionEngine", "Erro ao iniciar SpeechRecognizer", e)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.w("VoiceRecognitionEngine", "Erro ao destruir recognizer", e)
        } finally {
            _isListening.value = false
            _rmsDb.value = 0f
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _isListening.value = true
        _lastError.value = null
    }

    override fun onBeginningOfSpeech() {
        _isListening.value = true
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Normalize RMS dB typically in range [-2, 10] to [0.0, 1.0]
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
        _rmsDb.value = normalized
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _isListening.value = false
    }

    override fun onError(error: Int) {
        _isListening.value = false
        _rmsDb.value = 0f
        val errorMsg = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Erro na captura de áudio"
            SpeechRecognizer.ERROR_CLIENT -> "Erro do cliente de voz"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissão de microfone ausente"
            SpeechRecognizer.ERROR_NETWORK -> "Falha na rede de voz"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tempo esgotado na rede"
            SpeechRecognizer.ERROR_NO_MATCH -> "Nenhum comando reconhecido"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconhecedor ocupado"
            SpeechRecognizer.ERROR_SERVER -> "Erro no servidor de voz"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Nenhum som detectado"
            else -> "Erro no reconhecimento de voz ($error)"
        }
        _lastError.value = errorMsg
        Log.w("VoiceRecognitionEngine", "Speech error: $errorMsg")
    }

    override fun onResults(results: Bundle?) {
        _isListening.value = false
        _rmsDb.value = 0f
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val recognized = matches?.firstOrNull()?.trim()
        if (!recognized.isNullOrBlank()) {
            _partialResult.value = recognized
            // Filter wake-words if present, e.g. "Jo'On criar tarefa..." -> "criar tarefa..."
            val cleanedCommand = cleanWakeWords(recognized)
            onCommandRecognized?.invoke(cleanedCommand)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partial = matches?.firstOrNull()?.trim()
        if (!partial.isNullOrBlank()) {
            _partialResult.value = partial
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    private fun cleanWakeWords(text: String): String {
        return text
            .replace(Regex("^(jo'on|joon|jarvis|computador|assistente)\\s*", RegexOption.IGNORE_CASE), "")
            .trim()
            .ifEmpty { text }
    }
}
