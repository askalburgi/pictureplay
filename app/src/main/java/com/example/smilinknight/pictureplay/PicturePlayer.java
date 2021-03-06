package com.example.smilinknight.pictureplay;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.smilinknight.pictureplay.filter.AsyncResponse;
import com.example.smilinknight.pictureplay.filter.Filter;
import com.example.smilinknight.pictureplay.filter.FisheyeFilter;
import com.example.smilinknight.pictureplay.filter.MedianFilter;
import com.example.smilinknight.pictureplay.filter.PerformFilter;
import com.example.smilinknight.pictureplay.filter.MeanFilter;
import com.example.smilinknight.pictureplay.filter.SwirlFilter;
import com.example.smilinknight.pictureplay.helpers.MegaGestureListener;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

public class PicturePlayer extends AppCompatActivity {

    Context PicturePlayerContext = this;
    ImageView image;
    int undo_allowance;
    Deque<Bitmap> versions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        Intent i = getIntent();
        Uri image_uri = i.getData();
        image = (ImageView) findViewById(R.id.imageView);
        image.setImageURI(image_uri);
        undo_allowance = i.getIntExtra("undo_allowance", 1);
        versions = new LinkedBlockingDeque<>();

        image.setOnTouchListener(new MegaGestureListener(this) {

            @Override
            public void onSwipeRight() {
                if (versions.size()>0) {
                    Toast.makeText(PicturePlayer.this, "Undoing last action...", Toast.LENGTH_SHORT).show();
                    image.setImageBitmap(versions.removeLast());
                } else {
                    Toast.makeText(PicturePlayer.this, "No more undo's available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSwipeDown() {
                Toast.makeText(PicturePlayer.this, "Calculating Median of Pixels...", Toast.LENGTH_SHORT).show();
                applyFilter(new MedianFilter());
            }

            @Override
            public void onSwipeUp() {
                Toast.makeText(PicturePlayer.this, "Calculating Mean of Pixels...", Toast.LENGTH_SHORT).show();
                applyFilter(new MeanFilter());
            }

            @Override
            public void onDoubleTap(MotionEvent e) {
                Toast.makeText(PicturePlayer.this, "Swirling...", Toast.LENGTH_SHORT).show();
                applyFilter(new SwirlFilter());
            }

            @Override
            public void onLongPress (MotionEvent event) {
                Toast.makeText(PicturePlayer.this, "Making FishEye...", Toast.LENGTH_SHORT).show();
                applyFilter(new FisheyeFilter());
            }
        });

        Button save = (Button) findViewById(R.id.savebutton);
        save.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Toast.makeText(PicturePlayer.this, "Saving image...", Toast.LENGTH_SHORT).show();
                 image.setDrawingCacheEnabled(true);
                 MediaStore.Images.Media.insertImage(PicturePlayerContext.getContentResolver(), image.getDrawingCache(), "PICTUREPLAYER", "madebypictureplayer");
            }
         });


    }

    public void applyFilter(Filter filter) {
        saveVersion();
        BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
        (new PerformFilter(image, filter, new AsyncResponse() {
                @Override
                public void processFinish(Bitmap output) {
                    image.setImageBitmap(output);
                }
            }, this)).execute(drawable.getBitmap());
    }

    public void  saveVersion() {
        if (versions.size() >= undo_allowance) versions.removeFirst();
        versions.addLast(((BitmapDrawable) image.getDrawable()).getBitmap());
    }

}
