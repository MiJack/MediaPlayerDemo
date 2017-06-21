package cn.mijack.mediaplayerdemo.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Layout;
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
    private List<Song> data = new ArrayList<>();

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

    }

    @Override
    public int getItemCount() {
        System.out.println("size:" + data.size());
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
