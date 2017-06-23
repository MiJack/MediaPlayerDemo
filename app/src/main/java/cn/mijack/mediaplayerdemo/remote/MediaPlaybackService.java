package cn.mijack.mediaplayerdemo.remote;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.mijack.mediaplayerdemo.ui.MainActivity;

/**
 * @author Mr.Yuan
 * @date 2017/6/20
 */
public class MediaPlaybackService extends MediaBrowserServiceCompat {
    private static final String LOG_TAG = "LOG_TAG";

    private static final String TAG = MediaPlaybackService.class.getSimpleName();

    public static final String MEDIA_ID_ROOT = "__ROOT__";
    public static final String MEDIA_ID_EMPTY_ROOT = "__EMPTY__";
    private static final int REQUEST_CODE = 1;

    private MediaSessionCompat mSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        super.onCreate();
        Log.d(TAG, "onCreate");

//        mMusicProvider = new MusicProvider();

        // Start a new MediaSession.
        mSession = new MediaSessionCompat(this, TAG);
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

//        mPlayback = new Playback(this, mMusicProvider);
//        mPlayback.setCallback(new Playback.Callback() {
//            @Override
//            public void onPlaybackStatusChanged(int state) {
//                updatePlaybackState(null);
//            }
//
//            @Override
//            public void onCompletion() {
//                // In this simple implementation there isn't a play queue, so we simply 'stop' after
//                // the song is over.
//                handleStopRequest();
//            }
//
//            @Override
//            public void onError(String error) {
//                updatePlaybackState(error);
//            }
//        });

        Context context = getApplicationContext();

        // This is an Intent to launch the app's UI, used primarily by the ongoing notification.
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mSession.setSessionActivity(pi);

//        mNotificationManager = NotificationManagerCompat.from(this);
//        mAudioBecomingNoisyReceiver = new AudioBecomingNoisyReceiver(this);
//
//        updatePlaybackState(null);
//        // Create a MediaSessionCompat
//        mMediaSession = new MediaSessionCompat(this, LOG_TAG);
//
//        // Enable callbacks from MediaButtons and TransportControls
//        mMediaSession.setFlags(
//                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
//                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
//
//        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
//        mStateBuilder = new PlaybackStateCompat.Builder()
//                .setActions(
//                        PlaybackStateCompat.ACTION_PLAY |
//                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
//        mMediaSession.setPlaybackState(mStateBuilder.build());
//
//        // MySessionCallback() has methods that handle callbacks from a media controller
////        mMediaSession.setCallback(new MySessionCallback());
//
//        // Set the session's token so that client activities can communicate with it.
//        setSessionToken(mMediaSession.getSessionToken());
//        Log.d(TAG, "onCreate: ");
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // Verify the client is authorized to browse media and return the root that
        // makes the most sense here. In this example we simply verify the package name
        // is the same as ours, but more complicated checks, and responses, are possible
        if (!clientPackageName.equals(getPackageName())) {
            // Allow the client to connect, but not browse, by returning an empty root
            return new BrowserRoot(MEDIA_ID_EMPTY_ROOT, null);
        }
        return new BrowserRoot(MEDIA_ID_ROOT, null);
//        }
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mSession, startIntent);
        System.out.println("action:" + startIntent.getAction());
        return super.onStartCommand(startIntent, flags, startId);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

////  Browsing not allowed
//        if (TextUtils.isEmpty(parentMediaId)) {
//            result.sendResult(null);
//            return;
//        }

        // Assume for example that the music catalog is already loaded/cached.

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        // Check if this is the root menu:
//        if (MY_MEDIA_ROOT_ID.equals(parentMediaId)) {
//
//            // build the MediaItem objects for the top level,
//            // and put them in the mediaItems list
//        } else {
//
//            // examine the passed parentMediaId to see which submenu we're at,
//            // and put the children of that menu in the mediaItems list
//        }
        result.sendResult(mediaItems);
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.d(TAG, "playFromMediaId mediaId:" + mediaId + "  extras=" + extras);

            // The mediaId used here is not the unique musicId. This one comes from the
            // MediaBrowser, and is actually a "hierarchy-aware mediaID": a concatenation of
            // the hierarchy in MediaBrowser and the actual unique musicID. This is necessary
            // so we can build the correct playing queue, based on where the track was
            // selected from.
//            MediaMetadataCompat media = mMusicProvider.getMusic(mediaId);
//            if (media != null) {
//                mCurrentMedia =
//                        new MediaSessionCompat.QueueItem(media.getDescription(), media.hashCode());
//
//                // play the music
//                handlePlayRequest();
//            }
        }

        @Override
        public void onPlay() {
            Log.d(TAG, "play");

//            if (mCurrentMedia != null) {
//                handlePlayRequest();
//            }
        }

        @Override
        public void onSeekTo(long position) {
            Log.d(TAG, "onSeekTo:" + position);
//            mPlayback.seekTo((int) position);
        }

        @Override
        public void onPause() {
//            Log.d(TAG, "pause. current state=" + mPlayback.getState());
//            handlePauseRequest();
        }

        @Override
        public void onStop() {
//            Log.d(TAG, "stop. current state=" + mPlayback.getState());
//            handleStopRequest();
        }
    }
}
