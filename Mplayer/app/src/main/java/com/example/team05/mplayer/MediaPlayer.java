package com.example.team05.mplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;


public class MediaPlayer extends AppCompatActivity {
    private static final String TAG="MEDIA_PLAYER";
    SimpleExoPlayerView playerView;
    SimpleExoPlayer player;
    private boolean playWhenReady;
    private int resumeWindow;
    private long resumePosition;

    private DefaultBandwidthMeter BANDWIDTH_METER;
    private TrackSelector trackSelector;

    private DataSource.Factory mediaDataSourceFactory;
    private MediaSource mediaSource;

    private Uri uri;
    private Button retryButton;
    private TextView debugText;

    private Handler mainHandler;
    private EventLogger eventLogger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"Device on create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        mainHandler = new Handler();
        retryButton=(Button)findViewById(R.id.button);
        debugText=(TextView) findViewById(R.id.textView3);

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
            public void onBandwidthSample(int elapsedMs, long bytes, final long bitrate) {
                final String currentBandwidth=getBitRate(bitrate);
                Log.d("EventLogger",currentBandwidth);
                debugText.post(new Runnable() {
                    @Override
                    public void run() {
                        debugText.setText(currentBandwidth);
                    }
                });
            }

            private String getBitRate(long bitrate){
                String result="Bitrate: ";
                long Kbps=bitrate/1024;
                long Mbps=Kbps/1024;
                if(Mbps>0){
                    result+=Mbps+" Mbps";
                }else if(Kbps>0){
                    result+=Kbps+" Kbps";
                }else{
                    result+=bitrate+" Bps";
                }
                return result;
            }
        });

        // Adaptive Track Selector will determine the best track based on bandwidth.
        // The part needs to be read
        TrackSelection.Factory adaptiveTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);
        eventLogger = new EventLogger((MappingTrackSelector) trackSelector);

        // Create Renderer. Managing rendering
        DefaultRenderersFactory rendererFactory=new DefaultRenderersFactory(this);

        // Create Load Control. Managing buffer. Change buffer parameters
        DefaultLoadControl loadControl=new DefaultLoadControl();

        // Create the media player and set play attributes
        player = ExoPlayerFactory.newSimpleInstance(
                rendererFactory,
                trackSelector,
                loadControl);

        player.addListener(eventLogger);
        //player.addMetadataOutput(eventLogger);

        player.setAudioDebugListener(eventLogger);
        player.setVideoDebugListener(eventLogger);

        playerView.setPlayer(player);
        Log.d(TAG,"Current Window"+resumePosition);
        player.seekTo(resumePosition);
        player.setPlayWhenReady(playWhenReady);


        // Create media source to play
        mediaDataSourceFactory=new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "yourApplicationName"), BANDWIDTH_METER);

        // Parse media source based on format
        mediaSource = buildMediaSource(uri,"");

        // Start playing media
        player.prepare(mediaSource,false, false);
        

    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        Log.i(TAG,Integer.toString(type));
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource(uri, mediaDataSourceFactory,
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_SS:
                return new SsMediaSource(uri,mediaDataSourceFactory,
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }


    private void releasePlayer() {
        if (player != null) {
            updateResumePosition();
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


    public void rePlay(View view) {
        releasePlayer();
        initializePlayer();
    }


}
