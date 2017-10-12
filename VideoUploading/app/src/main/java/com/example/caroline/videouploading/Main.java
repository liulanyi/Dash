package com.example.caroline.videouploading;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Main extends AppCompatActivity {

    private Button takePictureButton;
    private Uri videoUri;
    private VideoView mVideoView ;

    File savedVideo;
    String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePictureButton = (Button) findViewById(R.id.uploadVideo);
        mVideoView = (VideoView) findViewById(R.id.videoView);
/*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            takePictureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }*/

        takePictureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    savedVideo = File.createTempFile("video1_", ".mp4", new File(Environment.getExternalStorageDirectory() + "/Movies"));
                    filePath = savedVideo.getPath();

                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                    videoUri = Uri.fromFile(savedVideo);

                    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);

                    //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                    //System.out.println(filePath);

                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "exp1 " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    static final int REQUEST_VIDEO_CAPTURE = 101;

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // check the resolution of the video. It is 720p
        /*MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
        mRetriever.setDataSource(filePath);
        Bitmap frame = mRetriever.getFrameAtTime();

        int width = frame.getWidth();
        int height = frame.getHeight();

        System.out.println("RRRRRRRRRRRRRRRRRRRR");
        System.out.println(width);
        System.out.println(height);*/

        mVideoView.setVideoPath(filePath);
        mVideoView.start();

    }
}
