package cn.mijack.mediaplayerdemo.remote;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.mijack.mediaplayerdemo.model.Song;
import cn.mijack.mediaplayerdemo.ui.MainActivity;

/**
 * @author admin
 * @date 2017/6/25
 */
public class MusicService extends MediaBrowserServiceCompat {
    private static final String TAG = "MusicService";
    private static final int REQUEST_CODE = 1;
    private static final int STOP_CMD = 2;
    private static final long STOP_DELAY = 30;
    private MediaSessionCompat mSession;
    private Playback mPlayback;
    private NotificationManagerCompat mNotificationManager;
    private AudioBecomingNoisyReceiver mAudioBecomingNoisyReceiver;
    private boolean mServiceStarted;
    private MediaSessionCompat.QueueItem mCurrentMedia;
    private Handler mDelayedStopHandler = new Handler(msg -> {
        if (msg == null || msg.what != STOP_CMD) {
            return false;
        }

//            if (!mPlayback.isPlaying()) {
//                Log.d(TAG, "Stopping service");
//                stopSelf();
//                mServiceStarted = false;
//            }
        return false;
    });
    private Song song;

    /*
       * (non-Javadoc)
       * @see android.app.Service#onCreate()
       */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

//        mMusicProvider = new MusicProvider();

        // Start a new MediaSession.
        mSession = new MediaSessionCompat(this, TAG);
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);


        mPlayback = new Playback(this);
        mPlayback.setCallback(new Playback.Callback() {
            @Override
            public void onPlaybackStatusChanged(int state) {
                updatePlaybackState(null);
            }

            @Override
            public void onCompletion() {
                // In this simple implementation there isn't a play queue, so we simply 'stop' after
                // the song is over.
                handleStopRequest();
            }

            @Override
            public void onError(String error) {
                updatePlaybackState(error);
            }
        });


        Context context = getApplicationContext();

        // This is an Intent to launch the app's UI, used primarily by the ongoing notification.
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mSession.setSessionActivity(pi);

        mNotificationManager = NotificationManagerCompat.from(this);
        mAudioBecomingNoisyReceiver = new AudioBecomingNoisyReceiver(this);
        updatePlaybackState(null);

    }

    private void updatePlaybackState(String error) {
        Log.d(TAG, "updatePlaybackState, playback state=" + mPlayback.getState());
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }

        long playbackActions = PlaybackStateCompat.ACTION_PLAY
                | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
        if (mPlayback.isPlaying()) {
            playbackActions |= PlaybackStateCompat.ACTION_PAUSE;
        }

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(playbackActions);

        int state = mPlayback.getState();

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(0, error);
            state = PlaybackStateCompat.STATE_ERROR;
        }

        // Because the playback state is pulled from the Playback class lint thinks it may not
        // match permitted values.
        //noinspection WrongConstant
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        // Set the activeQueueItemId if the current index is valid.
        if (mCurrentMedia != null) {
            stateBuilder.setActiveQueueItemId(mCurrentMedia.getQueueId());
        }

        mSession.setPlaybackState(stateBuilder.build());

        if (state == PlaybackStateCompat.STATE_PLAYING) {
            Log.d(TAG, "updatePlaybackState: playing");
//            Notification notification = postNotification();
//            startForeground(NOTIFICATION_ID, notification);
            mAudioBecomingNoisyReceiver.register();
        } else {
//            if (state == PlaybackStateCompat.STATE_PAUSED) {
//                postNotification();
//            } else {
//                mNotificationManager.cancel(NOTIFICATION_ID);
//            }
            stopForeground(false);
            mAudioBecomingNoisyReceiver.unregister();
        }
    }


    /**
     * (non-Javadoc)
     *
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mSession, startIntent);
        Log.d(TAG, "onStartCommand: ");
        return super.onStartCommand(startIntent, flags, startId);
    }

    /**
     * (non-Javadoc)
     *
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        // Service is being killed, so make sure we release our resources

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        // Always release the MediaSession to clean up resources
        // and notify associated MediaController(s).
        mSession.release();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("root", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(new ArrayList<>());
    }

    private void handlePlayRequest() {
        Log.d(TAG, "handlePlayRequest: mState=" + mPlayback.getState());

        if (mCurrentMedia == null) {
            // Nothing to play
            return;
        }

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        if (!mServiceStarted) {
            Log.v(TAG, "Starting service");
            // The MusicService needs to keep running even after the calling MediaBrowser
            // is disconnected. Call startService(Intent) and then stopSelf(..) when we no longer
            // need to play media.
            startService(new Intent(getApplicationContext(), MusicService.class));
            mServiceStarted = true;
        }

        if (!mSession.isActive()) {
            mSession.setActive(true);
        }

        updateMetadata();
        mPlayback.play(song);
    }

    private void handleStopRequest() {
        Log.d(TAG, "handleStopRequest: mState=" + mPlayback.getState());
        mPlayback.stop();
        // reset the delayed stop handler.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessage(STOP_CMD);

        updatePlaybackState(null);
    } private void handlePauseRequest() {
        Log.d(TAG, "handlePauseRequest: mState=" + mPlayback.getState());
        mPlayback.pause();

        // reset the delayed stop handler.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(STOP_CMD, STOP_DELAY);
    }

    private void updateMetadata() {
//        MediaSessionCompat.QueueItem queueItem = mCurrentMedia;
//        String musicId = queueItem.getDescription().getMediaId();
        MediaMetadataCompat track = song.toMediaMetadata();
        // mMusicProvider.getMusic(musicId);
        Log.d(TAG, "updateMetadata: ");
//        final String trackId = track.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        mSession.setMetadata(track);

//        // Set the proper album artwork on the media session, so it can be shown in the
//        // locked screen and in other places.
//        if (track.getDescription().getIconBitmap() == null
//                && track.getDescription().getIconUri() != null) {
//            fetchArtwork(trackId, track.getDescription().getIconUri());
//            postNotification();
//        }
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        private static final String TAG = "MediaSessionCallback";

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            super.onCommand(command, extras, cb);
            Log.d(TAG, "onCommand: ");
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            Log.d(TAG, "onMediaButtonEvent: ");
            return super.onMediaButtonEvent(mediaButtonEvent);
        }

        @Override
        public void onPrepare() {
            super.onPrepare();
            Log.d(TAG, "onPrepare: ");
        }

        @Override
        public void onPrepareFromMediaId(String mediaId, Bundle extras) {
            super.onPrepareFromMediaId(mediaId, extras);
            Log.d(TAG, "onPrepareFromMediaId: ");

        }

        @Override
        public void onPrepareFromSearch(String query, Bundle extras) {
            super.onPrepareFromSearch(query, extras);
            Log.d(TAG, "onPrepareFromSearch: ");
        }

        @Override
        public void onPrepareFromUri(Uri uri, Bundle extras) {
            super.onPrepareFromUri(uri, extras);
            Log.d(TAG, "onPrepareFromUri: ");
        }

        @Override
        public void onPlay() {
            super.onPlay();
            Log.d(TAG, "onPlay: ");

            if (mCurrentMedia != null) {
                handlePlayRequest();
            }        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
            Log.d(TAG, "onPlayFromMediaId: ");
             song = (Song) extras.getSerializable("data");
            MediaMetadataCompat media = song.toMediaMetadata();
            if (media != null) {
                mCurrentMedia =
                        new MediaSessionCompat.QueueItem(media.getDescription(), media.hashCode());

                // play the music
                handlePlayRequest();
            }
        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            super.onPlayFromSearch(query, extras);
            Log.d(TAG, "onPlayFromSearch: ");
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
            Log.d(TAG, "onPlayFromUri: ");
        }

        @Override
        public void onSkipToQueueItem(long id) {
            super.onSkipToQueueItem(id);
            Log.d(TAG, "onSkipToQueueItem: ");
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d(TAG, "onPause: ");handlePauseRequest();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            Log.d(TAG, "onSkipToNext: ");
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            Log.d(TAG, "onSkipToPrevious: ");
        }

        @Override
        public void onFastForward() {
            super.onFastForward();
            Log.d(TAG, "onFastForward: ");
        }

        @Override
        public void onRewind() {
            super.onRewind();
            Log.d(TAG, "onRewind: ");
        }

        @Override
        public void onStop() {
            super.onStop();
            Log.d(TAG, "onStop: ");
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            Log.d(TAG, "onSeekTo: ");
        }

        @Override
        public void onSetRating(RatingCompat rating) {
            super.onSetRating(rating);
            Log.d(TAG, "onSetRating: ");
        }

        @Override
        public void onSetCaptioningEnabled(boolean enabled) {
            super.onSetCaptioningEnabled(enabled);
            Log.d(TAG, "onSetCaptioningEnabled: ");
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);
            Log.d(TAG, "onSetRepeatMode: ");
        }

        @Override
        public void onSetShuffleModeEnabled(boolean enabled) {
            super.onSetShuffleModeEnabled(enabled);
            Log.d(TAG, "onSetShuffleModeEnabled: ");
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            super.onCustomAction(action, extras);
            Log.d(TAG, "onCustomAction: ");
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            super.onAddQueueItem(description);
            Log.d(TAG, "onAddQueueItem: ");
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description, int index) {
            super.onAddQueueItem(description, index);
            Log.d(TAG, "onAddQueueItem: ");
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            super.onRemoveQueueItem(description);
            Log.d(TAG, "onRemoveQueueItem: ");
        }

        @Override
        public void onRemoveQueueItemAt(int index) {
            super.onRemoveQueueItemAt(index);
            Log.d(TAG, "onRemoveQueueItemAt: ");
        }

    }

    private class AudioBecomingNoisyReceiver extends BroadcastReceiver {
        private final Context mContext;
        private boolean mIsRegistered = false;

        private IntentFilter mAudioNoisyIntentFilter =
                new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

        protected AudioBecomingNoisyReceiver(Context context) {
            mContext = context.getApplicationContext();
        }

        public void register() {
            if (!mIsRegistered) {
                mContext.registerReceiver(this, mAudioNoisyIntentFilter);
                mIsRegistered = true;
            }
        }

        public void unregister() {
            if (mIsRegistered) {
                mContext.unregisterReceiver(this);
                mIsRegistered = false;
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
//                handlePauseRequest();
            }
        }
    }

}