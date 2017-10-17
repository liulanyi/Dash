package com.example.caroline.videotest;

import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {

    private SurfaceHolder mSurHolder ;
    private VideoView mVideoView ;

    File savedVideo = null;
    String filePath = null ;

    private Camera mCamera = null;


    // buttons
    private Button initBtn = null;
    private Button startBtn = null ;
    private Button stopBtn = null;
    private Button reviewBtn = null;
    private Button stopReviewBtn = null;
    private Button sendBtn = null;
    private TextView recordingMsg ;

    private static final String TAG = "RecordVideo";

    private MediaRecorder recorder = null;
    private String outputDirectory = null;


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
        sendBtn = (Button) findViewById(R.id.send);
        recordingMsg = (TextView) findViewById(R.id.recording);
        mVideoView = (VideoView) this.findViewById(R.id.videoView);

        outputDirectory = Environment.getExternalStorageDirectory() + "/Movies";

        try {
            savedVideo = File.createTempFile("video1_", ".mp4", new File(outputDirectory));

        }
        catch (Exception e){
            e.printStackTrace();
        }
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
            case R.id.send:
                // TODO SendToServer
                //askTitle();
                //askDescription();
                Intent askTitleAndDescription = new Intent(MainActivity.this, VideoTitleAndDescription.class);
                startActivity(askTitleAndDescription);
        }
    }


    private void initRecording() {
        if (recorder != null) {
            return;
        }


        if (savedVideo.exists())
            savedVideo.delete();
        filePath = savedVideo.getPath();
        Variables.setFilePath(filePath);

        try {
            mCamera.stopPreview();
            recorder = new MediaRecorder();
            recorder.setCamera(mCamera);
            mCamera.unlock();

            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            // video Settings

            recorder.setVideoSize(1280, 720); // 720p
            recorder.setVideoFrameRate(30) ; // 30 fps
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
            recordingMsg.setText("recording");
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
            recordingMsg.setText(" ");
            releaseCamera();
            startBtn.setEnabled(false);
            stopBtn.setEnabled(false);
            reviewBtn.setEnabled(true);
            sendBtn.setEnabled(true);
        }
    }

    private void playRecording() {
        MediaController mc = new MediaController(this);
        mVideoView.setMediaController(mc);
        mVideoView.setVideoPath(filePath);
        mVideoView.start();
        stopReviewBtn.setEnabled(true);
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
            Toast.makeText(getApplicationContext(), "surfaceCreated : " + e.getMessage(), Toast.LENGTH_LONG).show();
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
        sendBtn.setEnabled(false);
        if(!initCamera())
            finish();
    }


    @Override
    protected void onStop(){
        super.onStop();
        finish();
    }

    @Override
    protected void onPause(){
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        finish();
    }

    private boolean initCamera() {
        try{
            mCamera = Camera.open();
            Camera.Parameters camParams = mCamera.getParameters();
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

}
