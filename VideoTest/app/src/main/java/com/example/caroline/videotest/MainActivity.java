package com.example.caroline.videotest;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private Button takePictureButton;
    private Uri videoUri;
    private SurfaceView mSurView;
    private SurfaceHolder mSurHolder;
    private static Surface mSurface;
    private VideoView mVideoView;
    private SurfaceTexture surText;

    File savedVideo;
    String filePath;

    CameraDevice camera;
    CameraCaptureSession cameraCaptureSession;
    //private Camera mCamera = null;

    MediaCodec mMediaCodec;


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

        /*MediaRecorder recorder = new MediaRecorder();
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        // video Settings
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        recorder.setVideoSize(720, 1080); // 720p
        recorder.setCaptureRate(30); // 30 fps
        recorder.setVideoEncodingBitRate(5000000); // 5Mbps
        recorder.setOutputFile(filePath);
        recorder.setPreviewDisplay(mSurface);
        try {
            recorder.prepare();
            recorder.start();   // Recording is now started

            recorder.stop();
            recorder.reset();   // You can reuse the object by going back to setVideoSource() step
            recorder.release(); // Now the object cannot be reused
        } catch (Exception e){

        }*/

        mVideoView.setVideoPath(filePath);
        mVideoView.start();

    }

    /*public void setSurface(){
        mSurface = mMediaCodec.createInputSurface();
    }*/

    /*public void setCamera (){
        mCamera = Camera.open();
        Camera.s

    }*/
/*
    public void RecordVideo () {
        //setCamera();

        MediaRecorder recorder = new MediaRecorder();

        recorder.setCamera(mCamera);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);


        try {
            savedVideo = File.createTempFile("video4_", ".mp4", new File(Environment.getExternalStorageDirectory() + "/Movies"));
            filePath = savedVideo.getPath();


            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            recorder.setVideoSize(720, 1080); // 720p
            recorder.setCaptureRate(30); // 30 fps
            recorder.setVideoEncodingBitRate(5000000); // 5Mbps
            recorder.setOutputFile(filePath);
            recorder.setPreviewDisplay(mSurface);
            recorder.prepare();
            recorder.start();   // Recording is now started

            recorder.stop();
            recorder.reset();   // You can reuse the object by going back to setVideoSource() step
            recorder.release(); // Now the object cannot be reused

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "exp1 " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }*/
}
