package com.example.caroline.videotest;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.media.MediaCodec;
import android.media.MediaRecorder;
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
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {

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
    private Camera mCamera = null;

    MediaCodec mMediaCodec;

    // buttons
    private Button initBtn ;
    private Button startBtn ;
    private Button stopBtn ;
    private Button reviewBtn ;
    private Button stopReviewBtn ;
    private TextView recordingMsg ;

    private static final String TAG = "RecordVideo";

    private MediaRecorder recorder = null;
    private String outputDirectory ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // references of the buttons
        initBtn = (Button) findViewById(R.id.init);
        startBtn = (Button) findViewById(R.id.start);
        stopBtn = (Button) findViewById(R.id.stop);
        reviewBtn = (Button) findViewById(R.id.review);
        stopReviewBtn = (Button) findViewById(R.id.sreview);
        recordingMsg = (TextView) findViewById(R.id.recording);
        mVideoView = (VideoView) this.findViewById(R.id.videoView);

/*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            takePictureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }*/
/*
        takePictureButton.setOnClickListener(new View.OnClickListener() {

        startBtn.setOnClickListener(new View.OnClickListener() {
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
        });*/
    }

    public void buttonTapped (View view){
        switch (view.getId()){
            case R.id.init:
                initRecording();
                break;
            case R.id.start:
                startRecording();
                break;
            case R.id.stop:
                stopRecording();
                break;
            case R.id.review:
                playRecording();
                break;
            case R.id.sreview:
                stopPlayRecording();
                break;
        }
    }


    private void initRecording() {
        if (recorder != null) {
            return;
        }

        outputDirectory = Environment.getExternalStorageDirectory() + "/Movies";

        try {
            savedVideo = File.createTempFile("video1_", ".mp4", new File(outputDirectory));

        }
        catch (Exception e){
            e.printStackTrace();
        }

        if (savedVideo.exists())
            savedVideo.delete();
        filePath = savedVideo.getPath();

        try {
            mCamera.stopPreview();
            //mCamera.unlock();
            recorder = new MediaRecorder();
            //Toast.makeText(getApplicationContext(), "here", Toast.LENGTH_LONG).show();
            recorder.setCamera(mCamera);
            mCamera.unlock();
            //recorder.getSurface();
            //Toast.makeText(getApplicationContext(), "here2", Toast.LENGTH_LONG).show();
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            // video Settings

            recorder.setVideoSize(1280, 720); // 720p
            recorder.setVideoFrameRate(30) ; // 30 fps
            //recorder.setCaptureRate(30) ; // 30 fps
            recorder.setVideoEncodingBitRate(5000000); // 5Mbps
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            recorder.setPreviewDisplay(mSurHolder.getSurface());
            recorder.setOutputFile(filePath);

            recorder.prepare();
            initBtn.setEnabled(false);
            startBtn.setEnabled(true);
        }
        catch(Exception e){
            Toast.makeText(getApplicationContext(), "error init : " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void startRecording() {
        recorder.setOnInfoListener(this);
        recorder.setOnErrorListener(this);
        try{
            //Thread.sleep(1000);
            recorder.start();
            //recordingMsg.setText("recording");
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "error start : " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (recorder != null){
            recorder.setOnErrorListener(null);
            recorder.setOnInfoListener(null);

            try{
                recorder.stop();
            }
            catch (IllegalStateException e){
                e.printStackTrace();
            }
            releaseRecorder();
            //recordingMsg.setText(" ");
            releaseCamera();
            startBtn.setEnabled(false);
            stopBtn.setEnabled(false);
            reviewBtn.setEnabled(true);
        }
    }

    private void playRecording() {
        MediaController mc = new MediaController(this);
        mVideoView.setMediaController(mc);
        mVideoView.setVideoPath(filePath);
        mVideoView.start();
        stopBtn.setEnabled(true);
    }

    private void stopPlayRecording() {
        mVideoView.stopPlayback();
    }

    private void releaseRecorder() {
        if (recorder != null){
            recorder.release();
            recorder = null;
        }
    }

    private void releaseCamera() {
        if (mCamera != null){
            try {
                mCamera.reconnect();
            } catch (IOException e){
                e.printStackTrace();
            }
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(mSurHolder);
            mCamera.startPreview();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        initBtn.setEnabled(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {

    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {

    }

    @Override
    protected void onResume(){
        //Log.v(TAG, "in onResume");
        super.onResume();
        initBtn.setEnabled(false);
        startBtn.setEnabled(false);
        stopBtn.setEnabled(false);
        reviewBtn.setEnabled(false);
        stopReviewBtn.setEnabled(false);
        if(!initCamera())
            finish();
    }

    private boolean initCamera() {
        try{
            mCamera = Camera.open();
            //Camera.Parameters camParams = mCamera.getParameters();
            mCamera.lock();
            mSurHolder = mVideoView.getHolder();
            mSurHolder.addCallback(this);
            mSurHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    static final int REQUEST_VIDEO_CAPTURE = 101;

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        MediaRecorder recorder = new MediaRecorder();
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

        }

        mVideoView.setVideoPath(filePath);
        mVideoView.start();

    }
*/
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
