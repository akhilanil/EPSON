package com.example.akhil.epson;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.net.Uri;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by akhil on 5/4/17.
 */

public class GifImageView extends View {

    private InputStream gifInputStream;
    private Movie gifMovie;
    private int movieWidth, movieHeight;
    private long movieStart, movieDuration ;
    static public View view;
    private int mDrawLeftPos, mDrawTopPos;


    public GifImageView(Context context, View view){
        super(context);
        init(context);
        this.view = view;
    }

    public GifImageView(Context context) {
        super(context);
        init(context);
    }

    public GifImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public GifImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public static int getActivityHeight() {
        return view.findViewById(android.R.id.content).getHeight();
    }

    public static int getActivityWidth() {
        return view.findViewById(android.R.id.content).getWidth();
    }

    private void init(Context context) {
        setFocusable(true);
        gifInputStream = context.getResources().openRawResource(R.raw.loading);

        gifMovie = Movie.decodeStream(gifInputStream);

        movieWidth = gifMovie.width();
        movieHeight = gifMovie.height();
        movieDuration = gifMovie.duration();
    }

    public int getMovieWidth() {
        return movieWidth;
    }

    public int getMovieHeight() {
        return movieHeight;
    }

    public long  getMovieDuration() {
        return  movieDuration;
    }


    @Override
    protected void onDraw(Canvas canvas) {

        long now = SystemClock.uptimeMillis();



        if (movieStart == 0) {
            movieStart = now;
        }
        if (gifMovie != null) {

            int duration = gifMovie.duration();
            if (duration == 0) {
                duration = 1000;
            }

            int relTime = (int) ((now - movieStart) % duration);

            gifMovie.setTime(relTime);
            gifMovie.draw(canvas,mDrawLeftPos,this.getPaddingTop());

            /*double scaleX = (double) getActivityWidth() / (double) gifMovie.width();
            double scaleY = (double) getActivityHeight() / (double) gifMovie.height();
            Log.d("SCALEX", String.valueOf(scaleX));
            Log.d("SCALEY", String.valueOf(scaleY));
           // canvas.scale(5, 5);
            canvas.drawBitmap();

            gifMovie.draw(canvas, 0, 0);*/
            invalidate();




        }
    }


    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec )
    {
        int p_top = this.getPaddingTop(), p_bottom = this.getPaddingBottom();

        // Calculate new desired height
        final int desiredHSpec = MeasureSpec.makeMeasureSpec( movieHeight + p_top + p_bottom , MeasureSpec.EXACTLY );

        setMeasuredDimension( widthMeasureSpec, desiredHSpec );
        super.onMeasure( widthMeasureSpec, desiredHSpec );

        // Update the draw left position
        mDrawLeftPos = Math.max( ( this.getWidth() - movieWidth ) / 2, 0) ;
        mDrawTopPos = Math.max((this.getMovieHeight() - movieHeight  )/2, 0);
    }


    /*public void setGifImageResource(int id) {
        mInputStream = mContext.getResources().openRawResource(id);
        init();
    }

    public void setGifImageUri(Uri uri) {
        try {
            mInputStream = mContext.getContentResolver().openInputStream(uri);
            init();
        } catch (FileNotFoundException e) {
            Log.e("GIfImageView", "File not found");
        }
    }*/
}
