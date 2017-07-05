package cn.mijack.mediaplayerdemo.utils;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

/**
 * @author admin
 * @date 2017/6/24
 */

public class Utils {
    public static String toString(Bundle extras) {
        Set<String> keySet = extras.keySet();
        if (keySet == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String key : keySet) {
            sb.append(key)
                    .append(":")
                    .append(extras.get(key))
                    .append("\n");
        }
        return sb.toString();
    }

    public static int bitmapSize(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return data.getByteCount();
        } else {
            return data.getAllocationByteCount();
        }
    }

    public static String formatByteSize(int size) {
        //byte
        if (size < 1024) {
            return String.format("%d B", size);
        }
        //k
        size /= 1024;
        if (size < 1024) {
            return String.format("%d KB", size);
        }
        //m
        size /= 1024;
        if (size < 1024) {
            return String.format("%d MB", size);
        }
        //g
        size /= 1024;
        if (size < 1024) {
            return String.format("%d GB", size);
        }
        return null;
    }

    public static void close(Closeable... closeables) {
        if (closeables==null){return;}
        for (Closeable closeable:closeables){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
