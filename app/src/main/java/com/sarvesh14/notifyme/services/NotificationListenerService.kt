package com.sarvesh14.notifyme.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.LruCache
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.*
import com.sarvesh14.notifyme.Constants
import com.sarvesh14.notifyme.Constants.ACTION_CHANGE_LANGUAGE
import com.sarvesh14.notifyme.Constants.ACTION_MUTE_SERVICE
import com.sarvesh14.notifyme.Constants.ACTION_PAUSE_SERVICE
import com.sarvesh14.notifyme.Constants.ACTION_PLAY_NOTIFICATION
import com.sarvesh14.notifyme.Constants.ACTION_START_OR_RESUME_SERVICE
import com.sarvesh14.notifyme.Constants.ACTION_STOP_SERVICE
import com.sarvesh14.notifyme.Constants.ACTION_UNMUTE_SERVICE
import com.sarvesh14.notifyme.MainActivity
import com.sarvesh14.notifyme.R
import com.sarvesh14.notifyme.util.Language
import org.w3c.dom.Text
import java.util.*
private const val TAG = "NotificationListenerSer"
class NotificationListenerService
    : Service() {

    companion object{
        var blackListedPackages: MutableLiveData<ArrayList<String>> = MutableLiveData<ArrayList<String>>()
        var isNotificationMuted:Boolean = false
        var isServiceRunning:Boolean = false
        // For translator models
        private const val NUM_TRANSLATORS = 2
        var sourceLang = Language("en")
        var targetLang = Language("en")
        var targetLangCode:String = "en"

        private lateinit var mTTS: TextToSpeech
    }
    private lateinit var context: Context

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    private val modelManager: RemoteModelManager = RemoteModelManager.getInstance()
    private val translators =
        object : LruCache<TranslatorOptions, Translator>(NUM_TRANSLATORS){
            override fun create(options: TranslatorOptions?): Translator {
                return Translation.getClient(options)
            }

            override fun entryRemoved(
                evicted: Boolean,
                key: TranslatorOptions?,
                oldValue: Translator,
                newValue: Translator?
            ) {
//                    super.entryRemoved(evicted, key, oldValue, newValue)
                oldValue.close()
            }
        }


    val availableLanguages:List<Language> = TranslateLanguage.getAllLanguages().map {
        Language(it)
    }



    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")
        instantiateTTS(Locale.ENGLISH)

        val notification: Notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentTitle("NotificationListenerService")
            .setContentText("Language: $targetLangCode")
            .setSmallIcon(R.drawable.ic_notifications_active)
            .addAction(R.drawable.ic_mute, "Mute", null)
            .setContentIntent(getMainActivityPendingIntent())
            .build()

        startForeground(Constants.NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    Log.d(TAG, "start or resume service ")
                    isServiceRunning = true
                }
                ACTION_PAUSE_SERVICE -> {
                    Log.d(TAG, "pause service ")
                    isServiceRunning = false
                }
                ACTION_STOP_SERVICE -> {
                    Log.d(TAG, "stop service ")
                    isServiceRunning = false
                    stopForeground(true)
                    stopSelf()
                }
                ACTION_MUTE_SERVICE -> {
                    Log.d(TAG, "mute service ")
                    isNotificationMuted = true
                    releaseTTS()
                    updateNotification()
                }
                ACTION_UNMUTE_SERVICE -> {
                    Log.d(TAG, "un_mute service ")
                    isNotificationMuted = false
                    instantiateTTS(Locale.forLanguageTag(targetLangCode))
                    updateNotification()
                }
                ACTION_CHANGE_LANGUAGE -> {
                    val langCode: String = it.getStringExtra("langCode") ?: "en"
                    Log.d(TAG, "onStartCommand: Change language: langCode: " + langCode)
                    changeLanguage(langCode)
                    updateNotification()
                }
                ACTION_PLAY_NOTIFICATION -> {
                    Log.d(TAG, "onStartCommand: play notification")
                    playNotification(it)

                }
                else -> {
                    Log.d(TAG, "unknown intent: ")
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun playNotification(intent: Intent){
        val text = intent.getStringExtra("textData")
//        Log.d(TAG, "playNotification: speakign text:" + text)
//        speak(text!!)
        translateIfNeededAndSpeak(text!!)

    }

    private fun speak(text: String) {
        Log.d(TAG, "speak: ")
        if(!isNotificationMuted){
            Log.d(TAG, "speak: text" + text)
            mTTS.setPitch(1.0F)
            mTTS.setSpeechRate(1.0F)
            mTTS.speak(text, TextToSpeech.QUEUE_ADD, null)
        }
    }

    private fun instantiateTTS(language: Locale) {
        mTTS = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if(status == TextToSpeech.SUCCESS){
                val result:Int = mTTS.setLanguage(language)
                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                    Log.e(TAG, "TTS: Language not supported")
                }else{
//                    Log.d(TAG, "TTS: Successfully created ")
                    Log.d(TAG, "instantiateTTS: created instance of TTS with " + language)
//                    binding.btnSpeak.isEnabled = true
                }
            }else{
                Log.e(TAG, "TTS: Initialization failed")
            }
        })
    }

    private fun releaseTTS(){
        mTTS.stop()
        mTTS.shutdown()
        Log.d(TAG, "releaseTTS: shutdonwn complete")
    }

    private fun changeLanguage(langCode: String) {
        targetLangCode = langCode
        targetLang = Language(targetLangCode)
        Log.d(TAG, "changeLanguage: targetLangCode" + targetLangCode)
        mTTS.setLanguage(Locale.forLanguageTag(targetLangCode))
//        updateModelCache()
        updateNotification()
    }

    private fun updateNotification() {
        if(isNotificationMuted){
            Log.d(TAG, "muting service: currLang: " + targetLangCode)
            val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentTitle("Example Service")
                .setContentText("Language: $targetLangCode")
                .setSmallIcon(R.drawable.ic_notifications_inactive)
                .addAction(R.drawable.ic_unmute, "Unmute", null)
                .setContentIntent(getMainActivityPendingIntent())
                .build()

            Log.d(TAG, "calling notify")
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(Constants.NOTIFICATION_ID, notification)
            //
        }else{
            Log.d(TAG, "unmuting service: currLang: " + targetLangCode)
            val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentTitle("Example Service")
                .setContentText("Language: $targetLangCode")
                .setSmallIcon(R.drawable.ic_notifications_active)
                .addAction(R.drawable.ic_mute, "Mute", null)
                .setContentIntent(getMainActivityPendingIntent())
                .build()
            Log.d(TAG, "calling notify")
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(Constants.NOTIFICATION_ID, notification)
            //
        }
    }
    private fun translateIfNeededAndSpeak(text: String) {
//        val packageName:String = statusBarNotification.packageName

        if(sourceLang.code != targetLang.code){
            translate2(text)
        }else{
            speak(text)
        }
//        packageName.let { currentPackageName ->
//            text = StringBuilder().apply {
//                append("Notification from ")
//                append(currentPackageName)
//            }.toString()
//            Log.d(TAG, "translateIfNeededAndSpeak: sourceLang:" + sourceLang.code + " targetLang:" + targetLang.code)
//            if(sourceLang.code != targetLang.code){
////                 translate(text).addOnCompleteListener{ task ->
////                    if(task.isSuccessful){
////                       text = task.result
////                    }else{
////                       text = "Unable to translate"
////                    }
////                }
//                translate2(text)
////                translate3(text)
//                return
//            }
//            speak(text)
////            speak(packageName)
//
//        }
    }


    fun translate2(text:String) {

//        val text = sourceText.value
//        val source = sourceLang
//        val target = targetLang
        if(text.isEmpty()){
            return
        }
        Log.d(TAG, "translate2: text"+ text)
        val sourceLangCode = TranslateLanguage.fromLanguageTag(sourceLang.code)!!
        val targetLangCode = TranslateLanguage.fromLanguageTag(targetLang.code)!!
        Log.d(TAG, "translate2: sourceLangCode:" + sourceLangCode + " targetLangCode:" + targetLangCode)
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLangCode)
            .setTargetLanguage(targetLangCode)
            .build()
        Log.d(TAG, "translate2: ###")
        var sol:String = ""
        translators[options].downloadModelIfNeeded().continueWithTask { task ->
            if(task.isSuccessful){
                translators[options].translate(text).addOnSuccessListener { translatedString ->
                    Log.d(TAG, "translate2: translatedString" + translatedString)
                    speak(translatedString)
                    fetchDownloadedModels()
                }
            }else{
                sol = "Unable to translate"
                Log.d(TAG, "translate2: Unable to translate")
                Tasks.forException<String>(
                    task.exception ?: Exception("Error occurred while translation")
                )
            }
        }
        Log.d(TAG, "translate2: ****")
    }
    private fun fetchDownloadedModels() {
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { remoteModels ->
                remoteModels.sortedBy { it.language }.map { it.language }
            }
    }
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_MAIN_ACTIVITY
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    override fun onDestroy() {
        super.onDestroy()
        mTTS.stop()
        mTTS.shutdown()
        translators.evictAll()
    }

}