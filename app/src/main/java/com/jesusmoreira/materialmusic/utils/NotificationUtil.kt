package com.jesusmoreira.materialmusic.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.media.app.NotificationCompat
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.models.PlaybackStatus
import com.jesusmoreira.materialmusic.services.PlaybackBroadcast.Companion.ACTION_NEXT
import com.jesusmoreira.materialmusic.services.PlaybackBroadcast.Companion.ACTION_PLAY_OR_PAUSE
import com.jesusmoreira.materialmusic.services.PlaybackBroadcast.Companion.ACTION_PREVIOUS
import com.jesusmoreira.materialmusic.ui.activities.MainActivity

object NotificationUtil {
    private const val TAG = "Notification"
    private const val NOTIFICATION_ID = 2000

    private const val CHANNEL_ID = "MediaPlayer"
    private const val CHANNEL_NAME = "MediaPlayer"
    private const val CHANNEL_DESCRIPTION = "MediaPlayer"

    private const val DEFAULT: Int = 2000
    private const val PLAY: Int = 2001
    private const val PAUSE: Int = 2002
    private const val PREVIOUS: Int = 2003
    private const val NEXT: Int = 2004

    fun buildNotification(context: Context, mediaSession: MediaSessionCompat?, /*audio: Audio?,*/ playbackStatus: PlaybackStatus) {
        var notificationAction = android.R.drawable.ic_media_pause // Needs to be initialized
        var playOrPauseAction: PendingIntent? = null

        // Build a new notification according to the current state of the MediaPlayer
        playOrPauseAction = when(playbackStatus) {
            PlaybackStatus.PLAYING -> {
                notificationAction = R.drawable.ic_pause_white_24dp
                // Create the pause action
                playbackAction(context, PAUSE)
            }
            PlaybackStatus.PAUSED -> {
                notificationAction = R.drawable.ic_play_arrow_white_24dp
                // Create the play action
                playbackAction(context, PLAY)
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

//        val bitmap = audio?.getAlbumArtBitmap(context)
//        val largeIcon = when {
//            bitmap != null -> bitmap
//            else -> GraphicUtil.getBitmapFromVectorDrawable(context, R.drawable.ic_album_black_24dp)
//        }


        val audioList = StorageUtil.audioListFromString(PreferenceUtil.getAudioList(context))
        val audioIndex = PreferenceUtil.getAudioIndex(context) ?: 0
        val audio = audioList[audioIndex]

        val notificationBuilder = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
            .setShowWhen(false)
            // Set the Notification style
            .setStyle(NotificationCompat.MediaStyle()
                // Attach our MediaSession token
                .setMediaSession(mediaSession?.sessionToken)
                // Show our playback controls in the compact notification view.
                .setShowActionsInCompactView(0, 1, 2))
            // Set the notification color
            .setColor(context.resources.getColor(R.color.grey, null))
            // Set large and small icons
            .setLargeIcon(audio.getAlbumArtBitmap(context))
            .setSmallIcon(R.drawable.ic_headset_white_24dp)
            // Set Notification content information
            .setContentText(audio.artist)
            .setContentTitle(audio.title)
            .setContentInfo(audio.album)
            .setContentIntent(playbackAction(context, null))
            // Add playback actions
            .addAction(R.drawable.ic_skip_previous_white_24dp, "previous", playbackAction(context, PREVIOUS))
            .addAction(notificationAction, "pause", playOrPauseAction)
            .addAction(R.drawable.ic_skip_next_white_24dp, "next", playbackAction(context, NEXT))

        if (context is Service) {
//            context.stopForeground(true)
            context.startForeground(NOTIFICATION_ID, notificationBuilder.build())
        } else {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }

    }

    fun removeNotification(context: Context) {
        if (context is Service) {
            context.stopForeground(true)
        } else {
            (context.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager)
                ?.cancel(NOTIFICATION_ID)
        }
    }

    private fun playbackAction(context: Context, action: Int?): PendingIntent? {
//        val playbackAction = Intent(context, MediaPlayerService::class.java)
//        Intent intent=new Intent("com.example.andy.CUSTOM_INTENT");
//        sendBroadcast(intent);

        return when (action) {
            PLAY -> {
                PendingIntent.getBroadcast(context, action, Intent(ACTION_PLAY_OR_PAUSE), 0)
            }
            PAUSE -> {
                PendingIntent.getBroadcast(context, action, Intent(ACTION_PLAY_OR_PAUSE), 0)
            }
            PREVIOUS -> {
                PendingIntent.getBroadcast(context, action, Intent(ACTION_PREVIOUS), 0)
            }
            NEXT -> {
                PendingIntent.getBroadcast(context, action, Intent(ACTION_NEXT), 0)
            }
            else -> {
                val notifyIntent= Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                PendingIntent.getActivity(context, DEFAULT, notifyIntent, 0)
            }
        }
    }
}