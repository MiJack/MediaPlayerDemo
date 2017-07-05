package cn.mijack.mediaplayerdemo.fragment;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.mijack.mediaplayerdemo.BuildConfig;
import cn.mijack.mediaplayerdemo.R;
import cn.mijack.mediaplayerdemo.adapter.MusicAdapter;
import cn.mijack.mediaplayerdemo.base.BaseFragment;
import cn.mijack.mediaplayerdemo.image.ImageLoader;
import cn.mijack.mediaplayerdemo.model.Song;
import cn.mijack.mediaplayerdemo.remote.MusicService;
import cn.mijack.mediaplayerdemo.ui.FullScreenPlayerActivity;
import cn.mijack.mediaplayerdemo.utils.Utils;
import cn.mijack.mediaplayerdemo.vm.MusicListViewModel;
import cn.mijack.mediaplayerdemo.vm.MusicPlayerStateViewModel;

/**
 * @author Mr.Yuan
 * @date 2017/6/20
 */
public class MusicListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final int REQUEST_CODE_PERMISSION_READ = 1;
    private static final int REQUEST_CODE_CHANGE_PERMISSION = 2;
    private static final String TAG = "MusicListFragment";
    MusicListViewModel musicListViewModel;
    private MusicAdapter adapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private ImageView music;
    private ImageView pervise;
    private ImageView playOrPause;
    private ProgressBar progressBar;
    private ImageView icNext;
    private TextView musicName;
    private TextView singer;
    Observer<List<Song>> observer = songs -> {
        refreshLayout.setRefreshing(false);
        adapter.setData(songs);
    };
    private MediaBrowserCompat mMediaBrowser;

    private MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {

                @Override
                public void onChildrenLoaded(String parentId,
                                             List<MediaBrowserCompat.MediaItem> children) {
//                    adapter.clear();
//                    adapter.notifyDataSetInvalidated();
//                    for (MediaBrowserCompat.MediaItem item : children) {
//                        adapter.add(item);
//                    }
//                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(String id) {
                    Toast.makeText(getActivity(), R.string.error_loading_media,
                            Toast.LENGTH_LONG).show();
                }
            };

    private String mMediaId;
    private MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "onConnected: session token " + mMediaBrowser.getSessionToken());

                    if (mMediaId == null) {
                        mMediaId = mMediaBrowser.getRoot();
                    }

                    mMediaBrowser.subscribe(mMediaId, mSubscriptionCallback);
                    try {
                        MediaControllerCompat mediaController =
                                new MediaControllerCompat(getActivity(),
                                        mMediaBrowser.getSessionToken());
                        MediaControllerCompat.setMediaController(getActivity(), mediaController);

                        // Register a Callback to stay in sync
                        mediaController.registerCallback(mControllerCallback);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Failed to connect to MediaController", e);
                    }
                }

                @Override
                public void onConnectionFailed() {
                    Log.e(TAG, "onConnectionFailed");
                }

                @Override
                public void onConnectionSuspended() {
                    Log.d(TAG, "onConnectionSuspended");
                    MediaControllerCompat mediaController = MediaControllerCompat
                            .getMediaController(getActivity());

                    if (mediaController != null) {
                        mediaController.unregisterCallback(mControllerCallback);
                        MediaControllerCompat.setMediaController(getActivity(), null);
                    }
                }
            };

    private Song song;
    private MediaControllerCompat.Callback mControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    Log.d(TAG, "onMetadataChanged: ");
                    if (metadata != null) {
                        adapter.setCurrentMediaMetadata(metadata);
                        String metadataId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                        if (TextUtils.isDigitsOnly(metadataId)) {
                            //
                            Uri uri = ContentUris.appendId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.buildUpon()
                                    , Long.parseLong(metadataId)).build();
                            ContentResolver contentResolver = getActivity().getContentResolver();
                            Cursor cursor = null;
                            try {
                                cursor = contentResolver.query(uri, null, null, null, null);
                                if (cursor.moveToFirst()) {
                                    song = Song.fromCursor(cursor);
                                    Log.d(TAG, "onMetadataChanged: song:" + song.toString());
                                    singer.setText(song.getArtist());
                                    musicName.setText(song.getTitle());
                                    ImageLoader.getInstance().loadMusicCover(music, song.getData());
//                                    progressBar.set
                                }
                            } finally {
                                Utils.close(cursor);
                            }
                        } else {
                            //error
                        }
                    }
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    adapter.setPlaybackState(state);
                    adapter.notifyDataSetChanged();
//                    musicPlayerStateViewModel.updatePlaybackState(state);
                }
            };
    private MusicPlayerStateViewModel musicPlayerStateViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        refreshLayout = view.findViewById(R.id.refreshView);
        music = view.findViewById(R.id.music);
        pervise = view.findViewById(R.id.pervise);
        playOrPause = view.findViewById(R.id.playOrPause);
        progressBar = view.findViewById(R.id.progressBar);
        icNext = view.findViewById(R.id.icNext);
        musicName = view.findViewById(R.id.musicName);
        singer = view.findViewById(R.id.singer);
        refreshLayout.setOnRefreshListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MusicAdapter(getActivity());
        recyclerView.setAdapter(adapter);
        musicListViewModel = ViewModelProviders.of(getActivity()).get(MusicListViewModel.class);
        musicPlayerStateViewModel = ViewModelProviders.of(getActivity()).get(MusicPlayerStateViewModel.class);
        loadData();
        mMediaBrowser = new MediaBrowserCompat(getActivity(),
                new ComponentName(getActivity(), MusicService.class),
                mConnectionCallback, null);
        music.setOnClickListener(view1 -> {
            Intent intent = new Intent(view1.getContext(),
                    FullScreenPlayerActivity.class);

            ActivityOptionsCompat activityOptions = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(getActivity(),
                            new Pair<>(music, "music"));
            ActivityCompat.startActivity(getActivity(), intent.putExtra("song",song), activityOptions.toBundle());

        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mMediaBrowser.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMediaBrowser.disconnect();
    }

    private void loadData() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(getActivity()).setTitle("请求权限")
                        .setTitle("截图保持在SD卡中，需要存储权限，请前往设置打开权限重试")
                        .setCancelable(false)
                        .setPositiveButton("确定", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            startActivityForResult(intent, REQUEST_CODE_CHANGE_PERMISSION);
                            dialog.dismiss();
                        })
                        .setNegativeButton("取消",
                                (dialog, which) -> Toast.makeText(getActivity(), "权限请求失败，无法截图", Toast.LENGTH_SHORT).show()).create().show();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_READ);
            }
        } else {
            musicListViewModel.loadData()
                    .observe(this, observer);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION_READ) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "开启权限失败", Toast.LENGTH_SHORT).show();
            } else {
                loadData();
            }
        }
    }

    @Override
    public void onRefresh() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(getActivity()).setTitle("请求权限")
                        .setTitle("截图保持在SD卡中，需要存储权限，请前往设置打开权限重试")
                        .setCancelable(false)
                        .setPositiveButton("确定", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            startActivityForResult(intent, REQUEST_CODE_CHANGE_PERMISSION);
                            dialog.dismiss();
                        })
                        .setNegativeButton("取消",
                                (dialog, which) -> Toast.makeText(getActivity(), "权限请求失败，无法截图", Toast.LENGTH_SHORT).show()).create().show();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_READ);
            }
        } else {
            musicListViewModel.reloadData()
                    .observe(this, observer);
        }
    }
}
