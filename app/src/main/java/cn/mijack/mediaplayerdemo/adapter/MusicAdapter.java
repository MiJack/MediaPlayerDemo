package cn.mijack.mediaplayerdemo.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.mijack.mediaplayerdemo.R;
import cn.mijack.mediaplayerdemo.image.ImageLoader;
import cn.mijack.mediaplayerdemo.model.Song;

/**
 * @author Mr.Yuan
 * @date 2017/6/21
 */
public class MusicAdapter extends RecyclerView.Adapter {
    private Activity activity;
    private List<Song> data = new ArrayList<>();
    private static final String TAG = "MusicAdapter";
    private String mCurrentMediaId;
    private PlaybackStateCompat mPlaybackState;

    public MusicAdapter(Activity listener) {
        this.activity = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false)) {
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView songName = (TextView) holder.itemView.findViewById(R.id.songName);
        TextView singer = (TextView) holder.itemView.findViewById(R.id.singer);
        ImageView coverart = (ImageView) holder.itemView.findViewById(R.id.musicIcon);
        ImageView icon = (ImageView) holder.itemView.findViewById(R.id.icon);
        Song song = data.get(position);
        String songId = String.valueOf(song.getId());
        boolean playing = songId.equals(getPlayingMediaId());
        icon.setVisibility(playing ? View.VISIBLE : View.INVISIBLE);
        songName.setText(song.getTitle());
        singer.setText(song.getArtist());

        ImageLoader.getInstance().loadMusicCover(coverart, song.getData());
        holder.itemView.setOnClickListener(v -> {
            boolean isPlaying = songId.equals(getPlayingMediaId());
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(activity);
            MediaControllerCompat.TransportControls controls = controller.getTransportControls();
            if (isPlaying) {
                controls.pause();
            } else {
                Bundle extras = new Bundle();
                extras.putSerializable("data", song);
                controls.playFromMediaId(songId, extras);
            }
        });
//            holder.mImageView.setImageDrawable(getContext().getResources()
//                    .getDrawable(playRes));
//            holder.mImageView.setVisibility(View.VISIBLE);
//        }
//        new Thread() {
//            @Override
//            public void run() {
//                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
//                mmr.setDataSource(song.getData());
//                byte[] data = mmr.getEmbeddedPicture();
//                Bitmap bitmap = data != null ? BitmapFactory.decodeByteArray(data, 0, data.length) :
//                        BitmapFactory.decodeResource(coverart.getResources(), R.drawable.ic_audiotrack);
//                coverart.post(() -> coverart.setImageBitmap(bitmap));
//            }
//        }.start();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Song> songs) {
        this.data.clear();
        if (songs != null) {
            data.addAll(songs);
        }
        this.notifyDataSetChanged();
    }

    @Nullable
    public String getPlayingMediaId() {
        boolean isPlaying = mPlaybackState != null
                && mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING;
        return isPlaying ? mCurrentMediaId : null;
    }

    public void setCurrentMediaMetadata(MediaMetadataCompat mediaMetadata) {
        mCurrentMediaId = mediaMetadata != null
                ? mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                : null;
    }

    public void setPlaybackState(PlaybackStateCompat playbackState) {
        mPlaybackState = playbackState;
    }

}
