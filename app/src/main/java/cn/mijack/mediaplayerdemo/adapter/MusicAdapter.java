package cn.mijack.mediaplayerdemo.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.mijack.mediaplayerdemo.R;
import cn.mijack.mediaplayerdemo.model.Song;

/**
 * @author Mr.Yuan
 * @date 2017/6/21
 */
public class MusicAdapter extends RecyclerView.Adapter {
    private Activity activity;
    private List<Song> data = new ArrayList<>();
    private String playingMediaId;
    private MediaMetadataCompat currentMediaMetadata;

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
        ImageView musicIcon = (ImageView) holder.itemView.findViewById(R.id.musicIcon);
        Song song = data.get(position);
        songName.setText(song.getTitle());
        singer.setText(song.getArtist());
        holder.itemView.setOnClickListener(v -> {
            MediaBrowserCompat.MediaItem item = getMediaItem(position);
            boolean isPlaying = TextUtils.equals(item.getMediaId(),String.valueOf(song.getId()));
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(activity);
            MediaControllerCompat.TransportControls controls = controller.getTransportControls();
            // If the item is playing, pause it, otherwise start it
            if (isPlaying) {
                controls.pause();
            } else {
                Bundle extras = new Bundle();
                extras.putString("data", song.getData());
                controls.playFromMediaId(item.getMediaId(), extras);
            }
        });
        boolean isPlay = TextUtils.isEmpty(playingMediaId) ? false : playingMediaId.equals(String.valueOf(song.getId()));
        musicIcon.setImageResource(isPlay ? R.drawable.ic_audiotrack_red : R.drawable.ic_audiotrack);
    }

    private String getPlayingMediaId() {
        return playingMediaId;
    }

    private MediaBrowserCompat.MediaItem getMediaItem(int position) {
        Song song = data.get(position);
        return new MediaBrowserCompat.MediaItem(new MediaDescriptionCompat.Builder()
                .setTitle(song.getTitle())
                .setMediaId(String.valueOf(song.getId()))
                .setDescription(song.getTitle() + "-" + song.getAlbum() + "(" + song.getDisplayName() + ")").build(),
                //flag 分为PLAYABLE和BROWSABLE
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        );
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

    public void setCurrentMediaMetadata(MediaMetadataCompat currentMediaMetadata) {
        this.currentMediaMetadata = currentMediaMetadata;
        if (currentMediaMetadata == null) {
            playingMediaId = null;
        } else {
            MediaDescriptionCompat description = currentMediaMetadata.getDescription();
            this.playingMediaId = description == null ? null : description.getMediaId();
        }
    }
}
