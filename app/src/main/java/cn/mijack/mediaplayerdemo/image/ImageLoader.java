package cn.mijack.mediaplayerdemo.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;

import cn.mijack.mediaplayerdemo.R;
import cn.mijack.mediaplayerdemo.utils.Utils;

/**
 * @author admin
 * @date 2017/6/24
 */

public class ImageLoader {
    private static final String TAG = "ImageLoader";
    private LruCache<String, Bitmap> lruCache;
    private static ImageLoader instance;

    private ImageLoader() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        int cacheSize = (int) (maxMemory / 8);
        Log.d(TAG, "ImageLoader: cacheSize:" + cacheSize);
        lruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
            }
        };
    }

    public static synchronized ImageLoader getInstance() {
        if (instance == null) {
            instance = new ImageLoader();
        }
        return instance;
    }

    public void loadMusicCover(ImageView coverart, String data) {
        LoadMusicCoverTask task = (LoadMusicCoverTask) coverart.getTag(R.id.task);
        if (task != null && TextUtils.equals(data, task.getData()) && task.isCancelled() == false) {
            return;
        }
        if (task != null) {
            task.cancel(true);
        }
        Bitmap bitmap = lruCache.get(data);
        coverart.setTag(R.id.path, data);
        if (bitmap != null) {
            coverart.setImageBitmap(bitmap);
            return;
        }
        LoadMusicCoverTask loadMusicCoverTask = new LoadMusicCoverTask(coverart, data);
        loadMusicCoverTask.execute();
    }

    public static class LoadMusicCoverTask extends AsyncTask<Void, Void, Bitmap> {
        private WeakReference<ImageView> imageView;
        private String data;

        public LoadMusicCoverTask(ImageView imageView, String data) {
            this.imageView = new WeakReference(imageView);
            this.data = data;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(data);
            byte[] data = mmr.getEmbeddedPicture();
            return data != null ? BitmapFactory.decodeByteArray(data, 0, data.length) :
                    null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                return;
            }
            ImageView imageView = this.imageView.get();
            if (imageView == null) {
                return;
            }
            Object tag = imageView.getTag(R.id.path);
            if (tag == null || !TextUtils.equals(tag.toString(), data)) {
                return;
            }

            if (bitmap == null) {
                imageView.setImageResource(R.drawable.ic_audiotrack);
            } else {
                imageView.setImageBitmap(bitmap);
                ImageLoader.getInstance().addBitmap(data, bitmap);
            }
        }

        public String getData() {
            return data;
        }
    }

    private void addBitmap(String data, Bitmap bitmap) {
        lruCache.put(data, bitmap);
    }
}
