package com.sharif.thunder.utils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.Message;
import okhttp3.*;

public class OtherUtil {

    public static String scrub(String string, boolean encodeBlank) {
        string = string.replaceAll("[!@#$%^&*(),.?\":{}|<>]", "");

        return encodeBlank ? string.replace(" ", "%20") : string;
    }

    public static void deleteMessageAfter(Message message, long delay) {
        message.delete().queueAfter(delay, TimeUnit.MILLISECONDS);
    }

    public static String loadResource(Object clazz, String name) {
        try (BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(clazz.getClass().getResourceAsStream(name)))) {
            StringBuilder sb = new StringBuilder();
            reader.lines().forEach(line -> sb.append("\r\n").append(line));
            return sb.toString().trim();
        } catch (IOException ex) {
            return null;
        }
    }

    public static InputStream imageFromUrl(String url) {
        if (url == null) return null;
        try {
            URL u = new URL(url);
            URLConnection urlConnection = u.openConnection();
            urlConnection.setRequestProperty(
                    "user-agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.112 Safari/537.36");
            return urlConnection.getInputStream();
        } catch (IOException | IllegalArgumentException ignore) {
        }
        return null;
    }
}
