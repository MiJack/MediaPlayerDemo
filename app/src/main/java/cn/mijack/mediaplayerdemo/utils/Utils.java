package cn.mijack.mediaplayerdemo.utils;

import android.os.Bundle;

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
}
