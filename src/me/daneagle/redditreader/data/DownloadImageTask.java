package me.daneagle.redditreader.data;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import me.daneagle.redditreader.R;
import me.daneagle.redditreader.Utils.LoaderImageView;

/**
 * Created by deagle on 7/16/13.
 */
public class DownloadImageTask extends AsyncTask<LoaderImageView, Void, Bitmap> {

    public static final int MAX_TEXTURE_SIZE = 1536;

    LoaderImageView imageView = null;

    @Override
    protected Bitmap doInBackground(LoaderImageView... imageViews) {
        this.imageView = imageViews[0];
        return download_Image((String) imageView.getTag());
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (result != null) imageView.setBitmap(result);
        else imageView.setBitmap(R.drawable.ic_picture);
    }


    private Bitmap download_Image(String url) {
        Bitmap bitmap = RemoteData.decodeSampleBitmapFromUrl(url, MAX_TEXTURE_SIZE,
                MAX_TEXTURE_SIZE);
        if (bitmap != null) return resize(bitmap);
        return bitmap;
    }

    private Bitmap resize(Bitmap image) {
        double width = image.getWidth();
        double height = image.getHeight();
        if (Math.max(width, height) > MAX_TEXTURE_SIZE) {
            double ratio = Math.max(width, height) / MAX_TEXTURE_SIZE;
            width = (width / ratio);
            height = (height / ratio);
        }
        return Bitmap.createScaledBitmap(image, (int)width-1, (int)height-1, false);
    }
}