package com.example.team05.mplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.w3c.dom.Text;

import java.util.EventListener;


public class MediaPlayer extends AppCompatActivity{
    private static final String TAG="MEDIA_PLAYER";
    SimpleExoPlayerView playerView;
    SimpleExoPlayer player;
    private boolean playWhenReady;
    private int currentWindow;
    private long playbackPosition;
    private DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private DebugTextViewHelper debugViewHelper;
    private TrackSelection.Factory videoTrackSelectionFactory;
    private DataSource.Factory mediaDataSourceFactory;
    private TrackSelector trackSelector;
    private MediaSource mediaSource;
    private int resumeWindow;
    private long resumePosition;
    private Uri uri;
    private Button retryButton;
    private Handler mainHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"Device on create");

        //Full Screen mode
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE); //Remove title bar
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //Remove notification bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        mainHandler = new Handler();
        //Set up player view.
        playerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
        playerView.requestFocus();
        //Get the intent info
        Intent intent=getIntent();
        uri=Uri.parse(intent.getStringExtra("uri"));
        String title=intent.getStringExtra("title");
        playWhenReady=true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            Log.i(TAG,"Device on start");
            initializePlayer();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG,"Device on resume");
        if ((Util.SDK_INT <= 23 || player == null)) {
            Log.i(TAG,"Device on resume");
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            Log.i(TAG,"Device on pause");
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            Log.i(TAG,"Device on stop");
            releasePlayer();
        }
    }


    private void initializePlayer(){

        BANDWIDTH_METER=new DefaultBandwidthMeter(mainHandler, new BandwidthMeter.EventListener() {
            @Override
            public void onBandwidthSample(int elapsedMs, long bytes, long bitrate) {
                //Log.i(TAG,Long.toString(bitrate));
                //Log.i(TAG,Long.toString(bytes));
                //Log.i(TAG,Integer.toString(elapsedMs));
            }
        });

        // Create Track Selector( When a media has multiple track to play, trackselector select one)
        TrackSelection.Factory adaptiveTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);

        // Create Renderer
        DefaultRenderersFactory rendererFactory=new DefaultRenderersFactory(this);

        // Create Load Control
        DefaultLoadControl loadControl=new DefaultLoadControl();

        // Create the media player and set play attributes
        player = ExoPlayerFactory.newSimpleInstance(
                rendererFactory,
                trackSelector,
                loadControl);
        playerView.setPlayer(player);
        player.setPlayWhenReady(playWhenReady);

        // Create media source to play
        mediaDataSourceFactory=new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "yourApplicationName"), BANDWIDTH_METER);
        mediaSource = buildMediaSource(uri,"");

        //mediaSource= new DashMediaSource(uri, dataSourceFactory,
        //        new DefaultDashChunkSource.Factory(mediaDataSourceFactory),null,null);

        // Start playing media
        player.prepare(mediaSource,true, false);

    }


    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        Log.i(TAG,Integer.toString(type));
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource(uri, mediaDataSourceFactory,
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, null);
            case C.TYPE_SS:
                return new SsMediaSource(uri,mediaDataSourceFactory,
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, null);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, null);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, null);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }



    //make it full screen
    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }



    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    private void updateResumePosition() {
        resumeWindow = player.getCurrentWindowIndex();
        resumePosition = Math.max(0, player.getCurrentPosition());
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }


}
