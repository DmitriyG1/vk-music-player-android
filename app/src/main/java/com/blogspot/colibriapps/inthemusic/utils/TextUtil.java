package com.blogspot.colibriapps.inthemusic.utils;

/**
 * Created by Dmitriy Gaiduk on 04.08.15.
 */
public class TextUtil {


    public static String makeTimeStringWithSeconds(int totalSeconds){
        int minutes = (int)(totalSeconds / 60.0);
        int seconds = (totalSeconds - minutes * 60);
        String result;

        result = leadStringWithChars(Integer.toString(minutes), '0', 2) +
                    ":" +
                    leadStringWithChars(Integer.toString(seconds), '0', 2);

        return result;
    }

    private static String leadStringWithChars(String str, char ch, int leadNum){
        String leadStr = "";
        int count;
        count = leadNum - str.length();
        for(int i = 0; i < count; i++){
            leadStr += ch;
        }
        return leadStr + str;
    }
}
