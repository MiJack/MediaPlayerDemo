package cn.mijack.mediaplayerdemo.remote;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr.Yuan
 * @date 2017/6/20
 */
public class MediaPlaybackService extends MediaBrowserServiceCompat {
    private static final String LOG_TAG = "LOG_TAG";
    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create a MediaSessionCompat
        mMediaSession = new MediaSessionCompat(this, LOG_TAG);

        // Enable callbacks from MediaButtons and TransportControls
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mMediaSession.setPlaybackState(mStateBuilder.build());

        // MySessionCallback() has methods that handle callbacks from a media controller
//        mMediaSession.setCallback(new MySessionCallback());

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mMediaSession.getSessionToken());
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // (Optional) Control the level of access for the specified package name.
//        // You'll need to write your own logic to do this.
//        if (allowBrowsing(clientPackageName, clientUid)) {
//            // Returns a root ID, so clients can use onLoadChildren() to retrieve the content hierarchy
//            return new BrowserRoot(MY_MEDIA_ROOT_ID, null);
//        } else {
        // Clients can connect, but since the BrowserRoot is an empty string
        // onLoadChildren will return nothing. This disables the ability to browse for content.
        return new BrowserRoot("", null);
//        }
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
}
