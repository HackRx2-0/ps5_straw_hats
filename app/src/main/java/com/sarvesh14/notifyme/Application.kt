package com.sarvesh14.notifyme

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build


class Application: Application(){


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val serviceChannel:NotificationChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT // We dont want to always make a noise when notification is updated. If defualt is selected It is going to make a lot of noise
            )
            val mNotificationManager:NotificationManager = getSystemService(NotificationManager::class.java) as NotificationManager
            mNotificationManager.createNotificationChannel(serviceChannel)

        }
    }

}