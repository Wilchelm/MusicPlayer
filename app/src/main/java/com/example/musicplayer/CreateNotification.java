package com.example.musicplayer;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.musicplayer.Services.NotificationActionService;

public class CreateNotification {
    public static final String CHANNEL_ID = "channel";
    public static final String ACTION_PREVIOUS = "actionprevious";
    public static final String ACTION_PLAY = "actionplay";
    public static final String ACTION_NEXT = "actionnext";
    public static Notification notification;

    @SuppressLint("MissingPermission")
    public static void createNotification(Context context, Track track, int icon, int pos, int size) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");

            PendingIntent previousPendingIntent;
            if (pos == 0){
                previousPendingIntent = null;
            }
            else {
                Intent intentPrevious = new Intent(context, NotificationActionService.class).setAction(ACTION_PREVIOUS);
                previousPendingIntent = PendingIntent.getBroadcast(context,0,intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            }

            Intent intentPlay = new Intent(context, NotificationActionService.class).setAction(ACTION_PLAY);
            PendingIntent playPendingIntent = PendingIntent.getBroadcast(context,0,intentPlay, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            PendingIntent nextPendingIntent;
            if (pos == 0){
                nextPendingIntent = null;
            }
            else {
                Intent intentNext = new Intent(context, NotificationActionService.class).setAction(ACTION_NEXT);
                nextPendingIntent = PendingIntent.getBroadcast(context,0,intentNext, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            }

            Bitmap largeIcon = track.getImage();
            if (largeIcon == null) {
                largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
            }

            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    //.setLargeIcon(largeIcon)
                    .setContentTitle(track.getTitle())
                    .setContentText(track.getArtist())
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .addAction(R.drawable.previous_icon, "Previous", previousPendingIntent)
                    .addAction(icon, "Play", playPendingIntent)
                    .addAction(R.drawable.next_icon, "Next", nextPendingIntent)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2, 3)
                            .setShowCancelButton(true)
                            .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(false)
                    .build();

            notificationManagerCompat.notify(1, notification);
        }
    }
}
