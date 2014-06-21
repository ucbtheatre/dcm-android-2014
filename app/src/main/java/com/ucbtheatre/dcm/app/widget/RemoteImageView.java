package com.ucbtheatre.dcm.app.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.ucbtheatre.dcm.app.R;


import java.net.URL;

/**
 * Created by kurtguenther.
 */
public class RemoteImageView extends ImageView {
    private static final String TAG = "ImageView";

    private Animation animation;
    private ScaleType realScaleType;

    public RemoteImageView(Context context){
        super(context);
    }

    public RemoteImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void startSpinnerAnimation() {
        //Save the original scale type
        this.realScaleType = getScaleType();

        //Set the image to be a spinner resource with specific scale
        Drawable spin = getContext().getResources().getDrawable(R.drawable.progress);
        setImageDrawable(spin);
        setScaleType(ScaleType.CENTER);

        //Set the spinner in motion
        //TODO: why do I have to set the Y value to slightly off-center, otherwise we get a wobble
        Animation rotation = new RotateAnimation(0.f, 360.f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.486f );
        rotation.setDuration(1000);
        rotation.setRepeatCount(Animation.INFINITE);
        this.startAnimation(rotation);

        this.animation = rotation;
    }

    protected void stopSpinnerAnimation()
    {
        //Stop spinner, restore the scale, set to the result.
        clearAnimation();
        setScaleType(realScaleType);
    }

    public static ImageLoader getLoader(Context context){

        if(!ImageLoader.getInstance().isInited()) {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                    .threadPriority(Thread.NORM_PRIORITY - 2)
                    .denyCacheImageMultipleSizesInMemory()
                    .discCacheFileNameGenerator(new Md5FileNameGenerator())
                    .tasksProcessingOrder(QueueProcessingType.LIFO)
                    .writeDebugLogs() // Remove for release app
                    .build();


            ImageLoader.getInstance().init(config);
        }

        return ImageLoader.getInstance();
    }

    public void loadURL(final URL url){

        if(url == null){
            Log.e("RemoteImageView", "Url is null");
            return;
        }

        startSpinnerAnimation();
        final String absoluteURL = url.getProtocol() + "://" + url.getHost() + url.getPath();
        DisplayImageOptions opts = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        ImageLoader loader = RemoteImageView.getLoader(getContext());
        loader.displayImage(absoluteURL, RemoteImageView.this, opts, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                //NOOP
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                stopSpinnerAnimation();
                setVisibility(GONE);
                invalidate();
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                stopSpinnerAnimation();
                invalidate();
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                stopSpinnerAnimation();
                invalidate();
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "Canceling image download due to remove from stack");
        getLoader(getContext()).cancelDisplayTask(this);
    }
}
