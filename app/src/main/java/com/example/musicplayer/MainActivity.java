package com.example.musicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SongChangeListener {

    private final List<Track> tracks = new ArrayList<>();
    private RecyclerView musicRecyclerView;
    private EditText searchText;
    private MediaPlayer mediaPlayer;
    private ImageView nextButton;
    private ImageView previousButton;
    private TextView endTime, startTime;
    private boolean isPlaying = false;
    private SeekBar playerSeekBar;
    private ImageView playPauseImage;
    private ImageView repeatImage;
    private ImageView cover;
    private Timer timer;
    private int currenSongListPosition = 0;
    private MusicAdapter musicAdapter;
    NotificationManager notificationManager;
    private boolean reapeatOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        LinearLayout searchButton = findViewById(R.id.searchButton);
        LinearLayout searchButton2 = findViewById(R.id.searchButton2);
        RelativeLayout searchBar = findViewById(R.id.searchBar);
        musicRecyclerView = findViewById(R.id.musicRecyclerView);
        final CardView playPauseCard = findViewById(R.id.playPauseCard);
        playPauseImage = findViewById(R.id.playPauseImage);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        playerSeekBar = findViewById(R.id.playerSeekBar);
        repeatImage = findViewById(R.id.repeatImage);
        searchText = findViewById(R.id.searchText);

        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);

        musicRecyclerView.setHasFixedSize(true);
        musicRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mediaPlayer = new MediaPlayer();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getMusicFilesFromFolder("Future House");
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.POST_NOTIFICATIONS}, 11);
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.POST_NOTIFICATIONS}, 11);
            }
        }

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchBar.getVisibility() == View.VISIBLE) {
                    searchBar.setVisibility(View.GONE);
                }
                else {
                    searchBar.setVisibility(View.VISIBLE);
                }
            }
        });

        repeatImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reapeatOnce == true) {
                    reapeatOnce = false;
                    repeatImage.setImageResource(R.drawable.repeat_icon);
                    return;
                }
                else {
                    reapeatOnce = true;
                    repeatImage.setImageResource(R.drawable.repeat_one_icon);
                    return;
                }
            }
        });

        searchButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = searchText.getText().toString();
                if (text.equals("")) {
                    getMusicFilesFromFolder("Future House");
                }
                else {
                    getSearchedMusicFiles(text);
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tracks.size() != 0) {
                    int nextSongListPosition = currenSongListPosition + 1;

                    if (nextSongListPosition >= tracks.size()) {
                        nextSongListPosition = 0;
                    }

                    tracks.get(currenSongListPosition).setPlaying(false);
                    tracks.get(nextSongListPosition).setPlaying(true);

                    musicAdapter.updateList(tracks);

                    musicRecyclerView.scrollToPosition(nextSongListPosition);
                    onChange(nextSongListPosition);
                }
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tracks.size() != 0) {
                    int previousSongListPosition = currenSongListPosition - 1;

                    if (previousSongListPosition < 0) {
                        previousSongListPosition = tracks.size() - 1;
                    }
                    tracks.get(currenSongListPosition).setPlaying(false);
                    tracks.get(previousSongListPosition).setPlaying(true);

                    musicAdapter.updateList(tracks);

                    musicRecyclerView.scrollToPosition(previousSongListPosition);
                    onChange(previousSongListPosition);
                }
            }
        });

        playPauseCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    isPlaying = false;
                    mediaPlayer.pause();
                    playPauseImage.setImageResource(R.drawable.play_icon);
                } else {
                    isPlaying = true;
                    mediaPlayer.start();
                    playPauseImage.setImageResource(R.drawable.pause_icon);
                }
            }
        });

        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (isPlaying) {
                        mediaPlayer.seekTo(progress);
                    } else {
                        mediaPlayer.seekTo(0);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
            registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"), RECEIVER_EXPORTED);
            startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#FF8C00"));
        }
    }

    @SuppressLint("Range")
    private void getMusicFilesFromFolder(String folderName) {
        tracks.clear();
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = contentResolver.query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.DATA + " LIKE '%/"+folderName+"/%'", null, null);

        if (cursor == null) {
            Toast.makeText(this, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
        } else if (!cursor.moveToNext()) {
            Toast.makeText(this, "No Music Found", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                final String getMusicFileName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                final String getMusicArtistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                long cursorId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

                Uri musicFileUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursorId);
                String getDuration = getDuration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                final Track track = new Track(getMusicFileName, getMusicArtistName, getDuration, false, null, musicFileUri);
                tracks.add(track);
            }

            Collections.sort(tracks, new MusicNameComparator());
            musicAdapter = new MusicAdapter(tracks, MainActivity.this);
            musicRecyclerView.setAdapter(musicAdapter);
        }
        cursor.close();
    }
    @SuppressLint("Range")
    private void getSearchedMusicFiles(String keyword) {
        tracks.clear();
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = contentResolver.query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.DATA + " LIKE '%/Future House/%'", null, null);

        if (cursor == null) {
            Toast.makeText(this, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
        } else if (!cursor.moveToNext()) {
            Toast.makeText(this, "No Music Found", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                final String getMusicFileName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                final String getMusicTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                final String getMusicArtistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                long cursorId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

                Uri musicFileUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursorId);
                String getDuration = getDuration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                String keywordLower = keyword.toLowerCase();
                boolean constains = false;
                String tmpMusicName = getMusicFileName.toLowerCase();
                String tmpMusicTtile = getMusicTitle.toLowerCase();
                String tmpMusicArtist = getMusicArtistName.toLowerCase();

                if (tmpMusicName.contains(keywordLower)) {
                    constains = true;
                }
                if (tmpMusicTtile.contains(keywordLower)) {
                    constains = true;
                }
                if (tmpMusicArtist.contains(keywordLower)) {
                    constains = true;
                }
                if (constains == true) {
                    final Track track = new Track(getMusicTitle, getMusicArtistName, getDuration, false, null, musicFileUri);
                    tracks.add(track);
                }
            }

            Collections.sort(tracks, new MusicNameComparator());
            musicAdapter = new MusicAdapter(tracks, MainActivity.this);
            musicRecyclerView.setAdapter(musicAdapter);
        }
        cursor.close();
    }

    @SuppressLint("Range")
    private void getMusicFiles() {
        tracks.clear();
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = contentResolver.query(uri, null, MediaStore.Audio.Media.DATA + " LIKE?", new String[]{"%.mp3%"}, null);

        if (cursor == null) {
            Toast.makeText(this, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
        } else if (!cursor.moveToNext()) {
            Toast.makeText(this, "No Music Found", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                final String getMusicFileName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                final String getMusicArtistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                long cursorId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

                Uri musicFileUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursorId);
                String getDuration = getDuration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                final Track track = new Track(getMusicFileName, getMusicArtistName, getDuration, false, null, musicFileUri);
                tracks.add(track);
            }

            Collections.sort(tracks, new MusicNameComparator());
            musicAdapter = new MusicAdapter(tracks, MainActivity.this);
            musicRecyclerView.setAdapter(musicAdapter);
        }
        cursor.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getMusicFilesFromFolder("Future House");
        } else {
            Toast.makeText(this, "Permissions Denied By User", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onChange(int position) {
        currenSongListPosition = position;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.reset();
        }

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(MainActivity.this, tracks.get(currenSongListPosition).getMusicFile());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Unable to play track", Toast.LENGTH_SHORT).show();
                }
            }
        }).start();

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                final int getTotalDuration = mp.getDuration();

                String generateDuration = String.format(Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(getTotalDuration),
                        TimeUnit.MILLISECONDS.toSeconds(getTotalDuration) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getTotalDuration))
                );

                endTime.setText(generateDuration);
                isPlaying = true;

                mp.start();

                playerSeekBar.setMax(getTotalDuration);
                playPauseImage.setImageResource(R.drawable.pause_icon);
            }
        });

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        final int getCurrentDuration = mediaPlayer.getCurrentPosition();

                        String generateDuration = String.format(Locale.getDefault(), "%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(getCurrentDuration),
                                TimeUnit.MILLISECONDS.toSeconds(getCurrentDuration) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrentDuration))
                        );

                        playerSeekBar.setProgress(getCurrentDuration);
                        startTime.setText(generateDuration);
                    }
                });
            }
        }, 1000, 1000);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.reset();

                timer.purge();
                timer.cancel();

                isPlaying = false;

                playPauseImage.setImageResource(R.drawable.play_icon);

                playerSeekBar.setProgress(0);

                if (tracks.size() != 0) {
                    if (reapeatOnce) {
                        tracks.get(currenSongListPosition).setPlaying(false);
                        tracks.get(currenSongListPosition).setPlaying(true);

                        musicAdapter.updateList(tracks);

                        musicRecyclerView.scrollToPosition(currenSongListPosition);
                        onChange(currenSongListPosition);
                    } else {
                        nextButton.performClick();
                    }
                }
            }
        });

        CreateNotification.createNotification(MainActivity.this, tracks.get(currenSongListPosition), R.drawable.pause_icon,1, tracks.size() -1);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");
            switch (action){
                case CreateNotification.ACTION_PREVIOUS:
                    onTrackPrevious();
                    break;
                case CreateNotification.ACTION_PLAY:
                    if(isPlaying){
                        onTrackPause();
                    }
                    else {
                        onTrackPlay();
                    }
                    break;
                case CreateNotification.ACTION_NEXT:
                    onTrackNext();
                    break;
            }
        }
    };
    @Override
    public void onTrackPrevious() {
        previousButton.performClick();
        CreateNotification.createNotification(MainActivity.this, tracks.get(currenSongListPosition), R.drawable.pause_icon,1, tracks.size() -1);
    }

    @Override
    public void onTrackPlay() {
        CreateNotification.createNotification(MainActivity.this, tracks.get(currenSongListPosition), R.drawable.pause_icon,1, tracks.size() -1);
        isPlaying = true;
        mediaPlayer.start();
        playPauseImage.setImageResource(R.drawable.pause_icon);
    }

    @Override
    public void onTrackPause() {
        CreateNotification.createNotification(MainActivity.this, tracks.get(currenSongListPosition), R.drawable.play_icon,1, tracks.size() -1);
        isPlaying = false;
        mediaPlayer.pause();
        playPauseImage.setImageResource(R.drawable.play_icon);
    }

    @Override
    public void onTrackNext() {
        nextButton.performClick();
        CreateNotification.createNotification(MainActivity.this, tracks.get(currenSongListPosition), R.drawable.pause_icon,1, tracks.size() -1);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                    "Channel", NotificationManager.IMPORTANCE_LOW);

            notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.cancelAll();
        }
        unregisterReceiver(broadcastReceiver);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.cancel(1);
    }

    public static Bitmap getEmbeddedCover(Context context, long cursorId, int targetWidth, int targetHeight) {
        Uri musicFileUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursorId);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, musicFileUri);

        byte[] coverBytes = retriever.getEmbeddedPicture();

        Bitmap coverBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);

        if (coverBytes != null) {
            coverBitmap = BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.length);
        }
        try {
            retriever.release();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Bitmap.createScaledBitmap(coverBitmap, targetWidth, targetHeight, false);
    }
}