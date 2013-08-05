package me.daneagle.redditreader.data;

import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Date;

/**
 * Created by deagle on 7/17/13.
 */
public class RedditCache {
    private static String cacheDir = "/Android/data/me.daneagle.redditreader/cache/";
    private static final long TIME_THRESHOLD = 1000 * 60 * 5; // 5 minutes

    static {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cacheDir = Environment.getExternalStorageDirectory() + cacheDir;
            File f = new File(cacheDir);
            f.mkdirs();
        }
    }

    static public String convertToCacheName(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(url.getBytes());
            byte[] b = digest.digest();
            BigInteger bi = new BigInteger(b);
            return "redditcache_" + bi.toString(32) + ".cac";
        } catch (Exception e) {
            Log.d("ERROR: ", e.toString());
            return null;
        }
    }

    private static boolean tooOld(long time) {
        long now = new Date().getTime();
        if (now - time > TIME_THRESHOLD) return true;
        return false;
    }

    public static byte[] read(String url) {
        try {
            String file = cacheDir + "/" + convertToCacheName(url);
            File f = new File(file);
            if (!f.exists() || f.length() < 1) return null;
            if (f.exists() && tooOld(f.lastModified())) f.delete();
            byte data[] = new byte[(int) f.length()];
            DataInputStream dis = new DataInputStream(new FileInputStream(f));
            dis.readFully(data);
            dis.close();
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    public static void write(String url, String data) {
        try {
            String file = cacheDir + "/" + convertToCacheName(url);
            PrintWriter pw = new PrintWriter(new FileWriter(file));
            pw.print(data);
            pw.close();
        } catch (Exception e) {
            Log.d("ERROR: ", e.toString());
        }
    }
}
