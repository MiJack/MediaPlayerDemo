package cn.mijack.mediaplayerdemo.model;

import android.database.Cursor;
import android.provider.MediaStore;

import java.io.Serializable;

/**
 * @author Mr.Yuan
 * @date 2017/6/20
 * <p>
 * _id:48417
 * _data:/storage/emulated/0/netease/cloudmusic/Music/呼斯楞 - 鸿雁.mp3
 * _display_name:呼斯楞 - 鸿雁.mp3
 * _size:4326943
 * mime_type:audio/mpeg
 * date_added:1496462818
 * is_drm:0
 * date_modified:1496462818
 * title:鸿雁
 * title_key:fCâ
 * duration:260023
 * artist_id:60
 * composer:null
 * album_id:65
 * track:8
 * year:null
 * is_ringtone:0
 * is_music:1
 * is_alarm:0
 * is_notification:0
 * is_podcast:0
 * bookmark:null
 * album_artist:呼斯楞
 * audio_tags:null
 * artist_id:1:60
 * artist_key:f9râ
 * artist:呼斯楞
 * album_id:1:65
 * album_key:fCâR21618443
 * album:鸿雁 . 塞北
 */
public class Song implements Serializable {


    private long id;
    private String data;
    private String displayName;
    private long size;
    private String mimeType;
    private long dateAdded;
    private long dateModified;
    private String title;
    private long duration;
    private long artistId;
    private long albumId;
    private String albumArtist;
    private String artist;
    private String album;

    public Song() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public static Song fromCursor(Cursor cur) {
        Song song = new Song();
        song.setId(cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media._ID)));
        song.setData(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA)));
        song.setDisplayName(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
        song.setSize(cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media.SIZE)));
        song.setMimeType(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)));
        song.setDateAdded(cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)));
        song.setDateModified(cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)));
        song.setTitle(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE)));
        song.setDuration(cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media.DURATION)));
        song.setArtistId(cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)));
        song.setAlbumId(cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
        song.setArtist(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
        song.setAlbum(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
        return song;
    }
}
