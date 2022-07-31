package com.rakuishi.narou.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.rakuishi.narou.BuildConfig
import com.rakuishi.narou.R
import com.rakuishi.narou.model.Novel

object NotificationHelper {

    private const val PREFIX = BuildConfig.APPLICATION_ID + ".channel."
    private const val CHANNEL_NEW_EPISODE = "${PREFIX}.new_episode"

    fun setupNotificationChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_NEW_EPISODE,
            context.getString(R.string.notification_channel_new_episode),
            NotificationManager.IMPORTANCE_LOW,
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        manager.createNotificationChannel(channel)
    }

    fun notifyNewEpisode(context: Context, novel: Novel) {
        // TODO: Create contentIntent to show NovelDetailScreen
        val builder = NotificationCompat.Builder(context, CHANNEL_NEW_EPISODE)
            .setSmallIcon(R.drawable.ic_new_episode_24)
            .setContentTitle(
                context.getString(
                    R.string.notification_channel_new_episode_description,
                    novel.title
                )
            )
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, builder.build())
    }
}