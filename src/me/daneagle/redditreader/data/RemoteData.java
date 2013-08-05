package me.daneagle.redditreader.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import me.daneagle.redditreader.Utils.Utils;

/**
 * Created by deagle on 7/17/13.
 */
public class RemoteData {

    public static HttpURLConnection getConnection(String url) {
        System.out.println("URL: " + url);
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setReadTimeout(30000); // Timeout at 30 seconds
            con.setRequestProperty("User-Agent", "Reddit Eagle");
        } catch (MalformedURLException e) {
            Log.e("getConnection()", "Invalid URL: " + e.toString());
        } catch (IOException e) {
            Log.e("getConnection()", "Could not connect: " + e.toString());
        }
        return con;
    }

    public static String readContents(String url) {
        byte[] t = RedditCache.read(url);
        String cached = null;
        if (t != null) {
            cached = new String(t);
            t = null;
        }
        if (cached != null) {
            Log.d("Message", "Using cache for " + url);
            return cached;
        }

        return requestContents(url);
    }

    public static String requestContents(String url) {
        HttpURLConnection con = getConnection(url);
        if (con == null) return null;
        try {
            StringBuffer sb = new StringBuffer(8192);
            String tmp = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((tmp = br.readLine()) != null) {
                sb.append(tmp).append("\n");
            }
            br.close();
            RedditCache.write(url, sb.toString());
            return sb.toString();
        } catch (IOException e) {
            Log.d("READ FAILED", e.toString());
            return null;
        }
    }

    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, null);
            System.out.println("IMAGE URL: " + url);
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    public static Drawable getDrawableFromUrl(final String url) {
        String filename = url;
        filename = filename.replace("/", "+");
        filename = filename.replace(":", "+");
        filename = filename.replace("~", "s");
        final File file = new File(Environment.getExternalStorageDirectory()
                + File.separator + filename);
        boolean exists = file.exists();
        if (!exists) {
            try {
                URL myFileUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) myFileUrl
                        .openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                final Bitmap result = BitmapFactory.decodeStream(is);
                is.close();
                new Thread() {
                    public void run() {
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        result.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                        try {
                            if (file.createNewFile()){
                                //
                            }
                            else{
                                //
                            }

                            FileOutputStream fo;
                            fo = new FileOutputStream(file);
                            fo.write(bytes.toByteArray());
                            fo.flush();
                            fo.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                BitmapDrawable returnResult = new BitmapDrawable(result);
                return returnResult;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        else {
            return new BitmapDrawable(BitmapFactory.decodeFile(file.toString()));
        }
    }


    public static Drawable decodeSampleDrawableFromUrl(String url, int reqWidth, int reqHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream is = null;
        try {
            is = (InputStream) new URL(url).getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BitmapFactory.decodeStream(is, null, options);
        options.inSampleSize = Utils.calculateInSampleSize(options, reqWidth, reqHeight);
        try {
            if (is.markSupported()) is.reset();
            else is = (InputStream) new URL(url).getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        options.inJustDecodeBounds = false;
        return new BitmapDrawable(BitmapFactory.decodeStream(is, null, options));
    }

    public static Bitmap decodeSampleBitmapFromUrl(String url, int reqWidth, int reqHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream is = null;
        try {
            is = (InputStream) new URL(url).getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BitmapFactory.decodeStream(is, null, options);
        options.inSampleSize = Utils.calculateInSampleSize(options, reqWidth, reqHeight);

        System.out.println("SAMPLE CALCULATED");
        try {
            //is.reset();
            //is.close();
            is = (InputStream) new URL(url).getContent();
            System.out.println("NEW STREAM URL: " + url);
            //is.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        options.inJustDecodeBounds = false;
        Bitmap b = BitmapFactory.decodeStream(is, null, options);
        return b;
    }
}
