package cn.mijack.mediaplayerdemo.vm;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

/**
 * Created by Mr.Yuan on 2017/7/5.
 */

public class MusicPlayerStateViewModel extends ViewModel {
    MutableLiveData<MusicPlayerState> musicPlayerStateLiveData = new MutableLiveData<MusicPlayerState>();

    public LiveData<MusicPlayerState> getMusicPlayerStateLiveData() {
        return musicPlayerStateLiveData;
    }

    public void updateMediaInfo(MediaMetadataCompat metadata) {
        musicPlayerStateLiveData.setValue(
                musicPlayerStateLiveData.getValue()
                        .setMetadata(metadata));
    }

    public void updatePlaybackState(PlaybackStateCompat state) {
        musicPlayerStateLiveData.setValue(
                musicPlayerStateLiveData.getValue()
                        .setState(state));
    }

    public static class MusicPlayerState {
        private MediaMetadataCompat metadata;
        private PlaybackStateCompat state;

        public MediaMetadataCompat getMetadata() {
            return metadata;
        }

        public MusicPlayerState setMetadata(MediaMetadataCompat metadata) {
            this.metadata = metadata;
            return this;
        }

        public PlaybackStateCompat getState() {
            return state;
        }

        public MusicPlayerState setState(PlaybackStateCompat state) {
            this.state = state;
            return this;
        }
    }
}
