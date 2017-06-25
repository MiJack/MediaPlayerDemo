package cn.mijack.mediaplayerdemo.remote;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
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
    private int mCurrentPosition;


    // Type of audio focus we have:
    private int mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
    private AudioManager mAudioManager;
    private final WifiManager.WifiLock mWifiLock;
    private String mCurrentMediaId;

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

    public void play(Song song) {
//        mPlayOnFocusGain = true;
//        tryToGetAudioFocus();
        String mediaId = String.valueOf(song.getId());
        boolean mediaHasChanged = !TextUtils.equals(mediaId, mCurrentMediaId);
        if (mediaHasChanged) {
            mCurrentPosition = 0;
            mCurrentMediaId = mediaId;
        }

        if (mState == PlaybackStateCompat.STATE_PAUSED
                && !mediaHasChanged && mMediaPlayer != null) {
            configMediaPlayerState();
        } else {
            mState = PlaybackStateCompat.STATE_STOPPED;
            relaxResources(false); // release everything except MediaPlayer
//            MediaMetadataCompat track = mMusicProvider.getMusic(item.getDescription().getMediaId());
//
//            String source = track.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
            try {
                createMediaPlayerIfNeeded();

                mState = PlaybackStateCompat.STATE_BUFFERING;

                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(song.getData());

                // Starts preparing the media player in the background. When
                // it's done, it will call our OnPreparedListener (that is,
                // the onPrepared() method on this class, since we set the
                // listener to 'this'). Until the media player is prepared,
                // we *cannot* call start() on it!
                mMediaPlayer.prepareAsync();

                // If we are streaming from the internet, we want to hold a
                // Wifi lock, which prevents the Wifi radio from going to
                // sleep while the song is playing.
                mWifiLock.acquire();

                if (callback != null) {
                    callback.onPlaybackStatusChanged(mState);
                }

            } catch (IOException ioException) {
                Log.e(TAG, "Exception playing song", ioException);
                if (callback != null) {
                    callback.onError(ioException.getMessage());
                }
            }
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
        Log.d(TAG, "onAudioFocusChange. focusChange=" + focusChange);
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // We have gained focus:
            mAudioFocus = AUDIO_FOCUSED;

        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS
                || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // We have lost focus. If we can duck (low playback volume), we can keep playing.
            // Otherwise, we need to pause the playback.
            boolean canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
            mAudioFocus = canDuck ? AUDIO_NO_FOCUS_CAN_DUCK : AUDIO_NO_FOCUS_NO_DUCK;

            // If we are playing, we need to reset media player by calling configMediaPlayerState
            // with mAudioFocus properly set.
            if (mState == PlaybackStateCompat.STATE_PLAYING && !canDuck) {
                // If we don't have audio focus and can't duck, we save the information that
                // we were playing, so that we can resume playback once we get the focus back.
//                mPlayOnFocusGain = true;
            }
        } else {
            Log.e(TAG, "onAudioFocusChange: Ignoring unsupported focusChange: " + focusChange);
        }
        configMediaPlayerState();
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
        Log.d(TAG, "onPrepared: ");
        configMediaPlayerState();
    }

    public void configMediaPlayerState() {
//        Log.d(TAG, "configMediaPlayerState. mAudioFocus=" + mAudioFocus);
//        if (mAudioFocus == AUDIO_NO_FOCUS_NO_DUCK) {
//            // If we don't have audio focus and can't duck, we have to pause,
//            if (mState == PlaybackStateCompat.STATE_PLAYING) {
////                pause();
//            }
//        } else {  // we have audio focus:
        if (mAudioFocus == AUDIO_NO_FOCUS_CAN_DUCK) {
            mMediaPlayer.setVolume(VOLUME_DUCK, VOLUME_DUCK); // we'll be relatively quiet
        } else {
            if (mMediaPlayer != null) {
                mMediaPlayer.setVolume(VOLUME_NORMAL, VOLUME_NORMAL); // we can be loud again
            } // else do something for remote client.
        }
        // If we were playing when we lost focus, we need to resume playing.
//            if (mPlayOnFocusGain) {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            Log.d(TAG, "configMediaPlayerState startMediaPlayer. seeking to "
                    + mCurrentPosition);
            if (mCurrentPosition == mMediaPlayer.getCurrentPosition()) {
                mMediaPlayer.start();
                mState = PlaybackStateCompat.STATE_PLAYING;
            } else {
                mMediaPlayer.seekTo(mCurrentPosition);
                mState = PlaybackStateCompat.STATE_BUFFERING;
            }
        }
//                mPlayOnFocusGain = false;
//            }
//        }
        if (callback != null) {
            callback.onPlaybackStatusChanged(mState);
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public void pause() {
        if (mState == PlaybackStateCompat.STATE_PLAYING) {
            // Pause media player and cancel the 'foreground service' state.
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentPosition = mMediaPlayer.getCurrentPosition();
            }
            // while paused, retain the MediaPlayer but give up audio focus
            relaxResources(false);
        }
        mState = PlaybackStateCompat.STATE_PAUSED;
        if (callback != null) {
            callback.onPlaybackStatusChanged(mState);
        }
    }

    private void relaxResources(boolean releaseMediaPlayer) {
        Log.d(TAG, "relaxResources. releaseMediaPlayer=" + releaseMediaPlayer);

//        mService.stopForeground(true);

        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        // we can also release the Wifi lock, if we're holding it
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
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