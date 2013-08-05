package me.daneagle.redditreader.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import me.daneagle.redditreader.R;
import me.daneagle.redditreader.data.Post;

/**
 * Created by deagle on 7/18/13.
 */
public class LoaderImageView extends RelativeLayout {
    private Context context;
    private AttributeSet attributeSet;
    private ProgressBar progressBar;
    private ResizableImageView imageView;
    //private Post post;

    public LoaderImageView(final Context context, final AttributeSet attrSet) {
        super(context, attrSet);
        this.context = context;
        this.attributeSet = attrSet;
        instantiate();
    }

    public void instantiate() {

        imageView = new ResizableImageView(context, attributeSet);
        progressBar = new ProgressBar(context, attributeSet);
        progressBar.setIndeterminate(true);

        addView(progressBar);
        addView(imageView);

        this.setGravity(Gravity.CENTER);

        imageView.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);
    }

    public void doneLoading(){
        imageView.setVisibility(VISIBLE);
        progressBar.setVisibility(GONE);
    }

    public void setDrawable(Drawable d){
        imageView.setImageDrawable(d);
        doneLoading();
    }

    public void setBitmap(Bitmap b){
        //post.setImage(b);
        imageView.setImageBitmap(b);
        doneLoading();
    }

    public void setBitmap(int resource){
        imageView.setImageDrawable(context.getResources().getDrawable(resource));
        doneLoading();
    }

}
