package cn.mijack.mediaplayerdemo.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;

import cn.mijack.mediaplayerdemo.R;
import cn.mijack.mediaplayerdemo.base.BaseActivity;
import cn.mijack.mediaplayerdemo.image.ImageLoader;
import cn.mijack.mediaplayerdemo.model.Song;

/**
 * Created by Mr.Yuan on 2017/7/5.
 */

public class FullScreenPlayerActivity extends BaseActivity {
    private ImageView imageView;
    private Song song;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_player);
        song = (Song) getIntent().getSerializableExtra("song");
        imageView = findViewById(R.id.image);
        ViewCompat.setTransitionName(imageView, "music");
        ImageLoader.getInstance().loadMusicCover(imageView, song.getData());
    }
}
