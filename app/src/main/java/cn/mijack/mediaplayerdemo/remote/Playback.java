package cn.mijack.mediaplayerdemo.remote;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.io.IOException;

import cn.mijack.mediaplayerdemo.model.Song;

/**
 * @author admin
 * @date 2017/6/25
 */
public class Playback implements AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener {

    // The volume we set the media player to when we lose audio focus, but are
    // allowed to reduce the volume instead of stopping playback.
    public static final float VOLUME_DUCK = 0.2f;
    // The volume we set the media player when we have audio focus.
    public static final float VOLUME_NORMAL = 1.0f;

    // we don't have audio focus, and can't duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    // we don't have focus, but can duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    // we have full audio focus
    private static final int AUDIO_FOCUSED = 2;
    private static final String TAG = "Playback";
    private MusicService musicService;
    private Callback callback;
    private int mState = PlaybackStateCompat.STATE_NONE;
    private MediaPlayer mMediaPlayer;
    private long mCurrentPosition;


    // Type of audio focus we have:
    private int mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
    private AudioManager mAudioManager;
    private final WifiManager.WifiLock mWifiLock;

    public Playback(MusicService musicService) {
        this.musicService = musicService;
        this.mAudioManager = (AudioManager) musicService.getSystemService(Context.AUDIO_SERVICE);

        // Create the Wifi lock (this does not acquire the lock, this just creates it).
        this.mWifiLock = ((WifiManager) musicService.getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "sample_lock");
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public int getState() {
        return mState;
    }

    public boolean isConnected() {
        return true;
    }

    public boolean isPlaying() {
        return /*mPlayOnFocusGain ||*/ (mMediaPlayer != null && mMediaPlayer.isPlaying());
    }


    public long getCurrentStreamPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : mCurrentPosition;
    }


    public void stop() {

    }

    public void play(MediaSessionCompat.QueueItem mCurrentMedia, Song song) {
        createMediaPlayerIfNeeded();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        String source = track.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
        try {
            mMediaPlayer.setDataSource(song.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Starts preparing the media player in the background. When
        // it's done, it will call our OnPreparedListener (that is,
        // the onPrepared() method on this class, since we set the
        // listener to 'this'). Until the media player is prepared,
        // we *cannot* call start() on it!
        mState = PlaybackStateCompat.STATE_BUFFERING;
        mMediaPlayer.prepareAsync();
        if (callback != null) {
            callback.onPlaybackStatusChanged(mState);
        }
    }


    private void createMediaPlayerIfNeeded() {
        Log.d(TAG, "createMediaPlayerIfNeeded. needed? " + (mMediaPlayer == null));
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();

            // Make sure the media player will acquire a wake-lock while
            // playing. If we don't do that, the CPU might go to sleep while the
            // song is playing, causing playback to stop.
            mMediaPlayer.setWakeMode(musicService.getApplicationContext(),
                    PowerManager.PARTIAL_WAKE_LOCK);

            // we want the media player to notify us when it's ready preparing,
            // and when it's done playing:
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
        } else {
            mMediaPlayer.reset();
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
//        configMediaPlayerState();
        mp.start();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    /* package */ interface Callback {
        /**
         * On current music completed.
         */
        void onCompletion();

        /**
         * on Playback status changed
         * Implementations can use this callback to update
         * playback state on the media sessions.
         */
        void onPlaybackStatusChanged(int state);

        /**
         * @param error to be added to the PlaybackState
         */
        void onError(String error);

    }

}