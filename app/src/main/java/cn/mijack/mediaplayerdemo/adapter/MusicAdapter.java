package cn.mijack.mediaplayerdemo.adapter;

import android.app.Activity;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
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
        Song song = data.get(position);
        songName.setText(song.getTitle());
        singer.setText(song.getArtist());
        holder.itemView.setOnClickListener(v -> {
            MediaBrowserCompat.MediaItem item = getMediaItem(position);
            boolean isPlaying = item.getMediaId().equals(getPlayingMediaId());
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(activity);
            MediaControllerCompat.TransportControls controls = controller.getTransportControls();
            // If the item is playing, pause it, otherwise start it
            if (isPlaying) {
                controls.pause();
            } else {
                controls.playFromMediaId(item.getMediaId(), null);
            }
        });
    }

    private String getPlayingMediaId() {
        return null;
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
}
