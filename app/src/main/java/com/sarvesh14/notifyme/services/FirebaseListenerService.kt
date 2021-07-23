package com.sarvesh14.notifyme.services

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sarvesh14.notifyme.Constants
import java.util.*


private const val TAG = "NotifyFirebaseListner"
class FirebaseListenerService: FirebaseMessagingService() {

//    companion object{
//        var blackListedPackages: MutableLiveData<ArrayList<String>> = MutableLiveData<ArrayList<String>>()
//        var isNotificationMuted:Boolean = false
//        // For translator models
//        private const val NUM_TRANSLATORS = 2
//        var sourceLang = Language("en")
//        var targetLang = Language("en")
//        var targetLangCode:String = "en"
//
//        private lateinit var mTTS: TextToSpeech
//    }
//    private lateinit var context: Context

    override fun onMessageReceived(message: RemoteMessage) {
//        super.onMessageReceived(message)
        Log.d(TAG, "onMessageReceived: message.getData:" + message.notification)
        Log.d(TAG, "onMessageReceived: message:" + (message.notification?.body ?: ""))
        sendCommandToService(message.notification?.body)
    }

    private fun sendCommandToService(message: String?) {
        Log.d(TAG, "sendCommandToService: message:" + message)
        if(message == null) return
        val intent:Intent = Intent(this, NotificationListenerService::class.java)
        intent.action = Constants.ACTION_PLAY_NOTIFICATION
        intent.putExtra("textData", message)
        Log.d(TAG, "sendCommandToService: callign startService")
        startService(intent)
    }


//    override fun onCreate() {
//        super.onCreate()
//
//        instantiateTTS(Locale.ENGLISH)
//
//        val notification: Notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
//            .setAutoCancel(false)
//            .setOngoing(true)
//            .setContentTitle("NotificationListenerService")
////            .setContentText("Language: $targetLangCode")
////            .setSmallIcon(R.drawable.ic_notifications_active)
////            .addAction(R.drawable.ic_mute, "Mute", getMutePendingIntent())
//            .setContentIntent(getMainActivityPendingIntent())
//            .build()
//
//        startForeground(Constants.NOTIFICATION_ID, notification)
//    }

//    private fun instantiateTTS(language: Locale) {
//        mTTS = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
//            if(status == TextToSpeech.SUCCESS){
//                val result:Int = mTTS.setLanguage(language)
//                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
//                    Log.e(TAG, "TTS: Language not supported")
//                }else{
////                    Log.d(TAG, "TTS: Successfully created ")
//                    Log.d(TAG, "instantiateTTS: created instance of TTS with " + language)
////                    binding.btnSpeak.isEnabled = true
//                }
//            }else{
//                Log.e(TAG, "TTS: Initialization failed")
//            }
//        })
//
//    }

//    private fun releaseTTS(){
//        mTTS.stop()
//        mTTS.shutdown()
//        Log.d(TAG, "releaseTTS: shutdonwn complete")
//    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//
//        intent?.let {
//            when(it.action){
//                ACTION_START_OR_RESUME_SERVICE -> {
//                    Log.d(TAG, "start or resume service ")
//                }
//                ACTION_PAUSE_SERVICE -> {
//                    Log.d(TAG, "pause service ")
//                }
//                ACTION_STOP_SERVICE -> {
//                    Log.d(TAG, "stop service ")
//                }
//                ACTION_MUTE_SERVICE -> {
//                    Log.d(TAG, "mute service ")
//                    isNotificationMuted = true
//                    releaseTTS()
//                    updateNotification()
//                }
//                ACTION_UNMUTE_SERVICE -> {
//                    Log.d(TAG, "un_mute service ")
//                    isNotificationMuted = false
//                    instantiateTTS(Locale.forLanguageTag(targetLangCode))
//                    updateNotification()
//                }
//                ACTION_CHANGE_LANGUAGE -> {
//                    val langCode: String = it.getStringExtra("langCode") ?: "en"
//                    Log.d(TAG, "onStartCommand: Change language: langCode: " + langCode)
//                    changeLanguage(langCode)
//                    updateNotification()
//                }
//                ACTION_PLAY_NOTIFICATION -> {
//                    playNotification(it)
//                }
//                else -> {
//                    Log.d(TAG, "unknown intent: ")
//                }
//            }
//        }
//        return START_NOT_STICKY
//    }

//    private fun playNotification(intent: Intent) {
//        val text:String = intent.getStringExtra("textData") ?: ""
//
//
//    }
//
//    private fun changeLanguage(langCode: String) {
//        targetLangCode = langCode
//        targetLang = Language(targetLangCode)
//        Log.d(TAG, "changeLanguage: targetLangCode" + targetLangCode)
//        mTTS.setLanguage(Locale.forLanguageTag(targetLangCode))
////        updateModelCache()
//        updateNotification()
//    }

//    private fun updateNotification() {
//        if(isNotificationMuted){
//            Log.d(TAG, "muting service: currLang: " + targetLangCode)
//            val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
//                .setAutoCancel(false)
//                .setOngoing(true)
//                .setContentTitle("Example Service")
//                .setContentText("Language: $targetLangCode")
////                .setSmallIcon(R.drawable.ic_notification_not_active)
////                .addAction(R.drawable.ic_unmute, "Unmute", null)
//                .setContentIntent(getMainActivityPendingIntent())
//                .build()
//
//            Log.d(TAG, "calling notify")
//            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.notify(Constants.NOTIFICATION_ID, notification)
//            //
//        }else{
//            Log.d(TAG, "unmuting service: currLang: " + targetLangCode)
//            val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
//                .setAutoCancel(false)
//                .setOngoing(true)
//                .setContentTitle("Example Service")
//                .setContentText("Language: $targetLangCode")
////                .setSmallIcon(R.drawable.ic_notifications_active)
////                .addAction(R.drawable.ic_mute, "Mute", null)
//                .setContentIntent(getMainActivityPendingIntent())
//                .build()
//            Log.d(TAG, "calling notify")
//            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.notify(Constants.NOTIFICATION_ID, notification)
//            //
//        }
//    }

//    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
//        this,
//        0,
//        Intent(this, MainActivity::class.java).also {
//            it.action = Constants.ACTION_SHOW_MAIN_ACTIVITY
//        },
//        PendingIntent.FLAG_UPDATE_CURRENT
//    )

//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }




}