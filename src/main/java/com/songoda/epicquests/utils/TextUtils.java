package com.songoda.epicquests.utils;

import java.util.ArrayList;
import java.util.List;

public class TextUtils extends com.songoda.core.utils.TextUtils {
    public static List<String> condense(String message) {
        ArrayList<String> lore = new ArrayList<>();
        if (message == null) {
            return lore;
        }
        int lastIndex = 0;
        for (int n = 0; n < message.length(); n++) {
            if (n - lastIndex < 20) {
                continue;
            }

            if (message.charAt(n) == ' ') {
                lore.add(formatText("&6" + message.substring(lastIndex, n).trim()));
                lastIndex = n;
            }
        }

        if (lastIndex - message.length() < 20) {
            lore.add(formatText("&6" + message.substring(lastIndex).trim()));
        }

        return lore;
    }
}
