package com.rakuishi.narou.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.rakuishi.narou.BuildConfig
import com.rakuishi.narou.R
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.ui.MainActivity

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
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            "narou://novels/${novel.id}/episodes/${novel.latestEpisodeNumber}".toUri(),
            context,
            MainActivity::class.java
        )
        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        val builder = NotificationCompat.Builder(context, CHANNEL_NEW_EPISODE)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_new_episode_24)
            .setContentTitle(
                context.getString(
                    R.string.notification_channel_new_episode_description,
                    novel.title
                )
            )
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(0, builder.build())
    }
}