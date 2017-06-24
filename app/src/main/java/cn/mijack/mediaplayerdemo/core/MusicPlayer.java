package cn.mijack.mediaplayerdemo.core;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import cn.mijack.mediaplayerdemo.remote.MediaPlaybackService;
import cn.mijack.mediaplayerdemo.utils.Utils;


/**
 * @author admin
 * @date 2017/6/24
 */

public class MusicPlayer extends MediaSessionCompat.Callback {
    private static final String TAG = "MusicPlayer";
    private AudioManager mAudioManager;
    private WifiManager.WifiLock mWifiLock;
    private MediaPlaybackService mediaBrowserService;
    private MediaPlayer player;

    public MusicPlayer(MediaPlaybackService mediaBrowserService) {
        this.mediaBrowserService = mediaBrowserService;
        player = new MediaPlayer();
        Context context = mediaBrowserService.getApplicationContext();
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        // Create the Wifi lock (this does not acquire the lock, this just creates it).
        this.mWifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "sample_lock");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        super.onPlayFromMediaId(mediaId, extras);
        Log.d(TAG, "onPlayFromMediaId: mediaId:" + mediaId);
        Log.d(TAG, "onPlayFromMediaId: extras:" + Utils.toString(extras));
        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(mediaBrowserService, Uri.fromFile(new File(extras.getString("data"))));
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //update
        mediaBrowserService.updateMetadata(mediaId, extras);
    }
}
