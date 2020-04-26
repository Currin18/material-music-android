package com.jesusmoreira.materialmusic.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.app.NotificationCompat
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.controllers.MediaPlayerService
import com.jesusmoreira.materialmusic.controllers.MediaPlayerService.Companion.ACTION_NEXT
import com.jesusmoreira.materialmusic.controllers.MediaPlayerService.Companion.ACTION_PAUSE
import com.jesusmoreira.materialmusic.controllers.MediaPlayerService.Companion.ACTION_PLAY
import com.jesusmoreira.materialmusic.controllers.MediaPlayerService.Companion.ACTION_PREVIOUS
import com.jesusmoreira.materialmusic.models.Audio
import com.jesusmoreira.materialmusic.models.PlaybackStatus
import org.jetbrains.anko.restrictionsManager

object NotificationUtil {
    private const val NOTIFICATION_ID = 1001

    private const val CHANNEL_ID = "MediaPlayer"
    private const val CHANNEL_NAME = "MediaPlayer"
    private const val CHANNEL_DESCRIPTION = "MediaPlayer"

    fun buildNotification(context: Context, mediaSession: MediaSessionCompat?, audio: Audio?, playbackStatus: PlaybackStatus) {
        var notificationAction = android.R.drawable.ic_media_pause // Needs to be initialized
        var playOrPauseAction: PendingIntent? = null

        // Build a new notification according to the current state of the MediaPlayer
        playOrPauseAction = when(playbackStatus) {
            PlaybackStatus.PLAYING -> {
                notificationAction = R.drawable.ic_pause_white_24dp
                // Create the pause action
                playbackAction(context, 1)
            }
            PlaybackStatus.PAUSED -> {
                notificationAction = R.drawable.ic_play_arrow_white_24dp
                // Create the play action
                playbackAction(context, 0)
            }
        }

//        val largeIcon = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.ic_launcher_foreground)

        // Create a new Notification
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && notificationManager.notificationChannels.none { it.id == CHANNEL_ID }) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = CHANNEL_DESCRIPTION
            channel.enableVibration(false)
            channel.enableLights(false)
            channel.setSound(null, null)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
            .setShowWhen(false)
            // Set the Notification style
            .setStyle(NotificationCompat.MediaStyle()
                // Attach our MediaSession token
                .setMediaSession(mediaSession?.sessionToken)
                // Show our playback controls in the compact notification view.
                .setShowActionsInCompactView(0, 1, 2))
            // Set the notification color
            .setColor(context.resources.getColor(R.color.colorPrimary, null))
            // Set large and small icons
            .setLargeIcon(audio?.albumArtBitmap)
            .setSmallIcon(R.drawable.ic_headset_white_24dp)
            // Set Notification content information
            .setContentText(audio?.artist)
            .setContentTitle(audio?.album)
            .setContentInfo(audio?.title)
            // Add playback actions
            .addAction(R.drawable.ic_skip_previous_white_24dp, "previous", playbackAction(context, 3))
            .addAction(notificationAction, "pause", playOrPauseAction)
            .addAction(R.drawable.ic_skip_next_white_24dp, "next", playbackAction(context, 2))

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

    }

    fun removeNotification(context: Context) {
        (context.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager)
            ?.cancel(NOTIFICATION_ID)
    }

    private fun playbackAction(context: Context, actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(context, MediaPlayerService::class.java)
        return when (actionNumber) {
            0 -> {
                // Play
                playbackAction.action = ACTION_PLAY
                PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
            1 -> {
                // Play
                playbackAction.action = ACTION_PAUSE
                PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
            2 -> {
                // Play
                playbackAction.action = ACTION_NEXT
                PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
            3 -> {
                // Play
                playbackAction.action = ACTION_PREVIOUS
                PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
            else -> null
        }
    }
}