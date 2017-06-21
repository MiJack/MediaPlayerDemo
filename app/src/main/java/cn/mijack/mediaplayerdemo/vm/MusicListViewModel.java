package cn.mijack.mediaplayerdemo.vm;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import cn.mijack.mediaplayerdemo.model.Song;

/**
 * @author Mr.Yuan
 * @date 2017/6/20
 */
public class MusicListViewModel extends AndroidViewModel {

    private MutableLiveData<List<Song>> songData;

    public MusicListViewModel(Application application) {
        super(application);
    }

    public LiveData<List<Song>> loadData() {
        if (songData == null) {
            songData = new MutableLiveData<>();
            new Thread(() -> {
                ContentResolver cr = getApplication().getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
                String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
                Cursor cur = cr.query(uri, null, selection, null, sortOrder);
                List<Song> songs = new ArrayList<>();
                if (cur != null) {
                    if (cur.moveToFirst()) {
                        do {
                            System.out.println("------------------------");
                            Song song = Song.fromCursor(cur);
                            songs.add(song);
                        } while (cur.moveToNext());
                    }
                }
                cur.close();
                songData.postValue(songs);

            }).start();
        }
        return songData;
    }
}
