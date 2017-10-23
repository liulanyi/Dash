package com.example.team05.mplayer;

/**
 * Created by lanyi on 15/10/2017.
 */
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataRenderer;
import com.google.android.exoplayer2.metadata.emsg.EventMessage;
import com.google.android.exoplayer2.metadata.id3.ApicFrame;
import com.google.android.exoplayer2.metadata.id3.CommentFrame;
import com.google.android.exoplayer2.metadata.id3.GeobFrame;
import com.google.android.exoplayer2.metadata.id3.Id3Frame;
import com.google.android.exoplayer2.metadata.id3.PrivFrame;
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame;
import com.google.android.exoplayer2.metadata.id3.UrlLinkFrame;
import com.google.android.exoplayer2.source.AdaptiveMediaSourceEventListener;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import java.io.IOException;
import java.security.acl.LastOwnerException;
import java.text.NumberFormat;
import java.util.Locale;
public final class EventLogger implements ExoPlayer.EventListener,AudioRendererEventListener,
        VideoRendererEventListener,AdaptiveMediaSourceEventListener,ExtractorMediaSource.EventListener,
        MetadataRenderer.Output{

    private static final String TAG="EventLogger";
    private final Timeline.Window window;
    private final Timeline.Period period;
    private final long startTimeMs;
    private final MappingTrackSelector trackSelector;


    private static final NumberFormat TIME_FORMAT;
    static {
        TIME_FORMAT = NumberFormat.getInstance(Locale.US);
        TIME_FORMAT.setMinimumFractionDigits(2);
        TIME_FORMAT.setMaximumFractionDigits(2);
        TIME_FORMAT.setGroupingUsed(false);
    }

    public EventLogger(MappingTrackSelector trackSelector){
        this.trackSelector=trackSelector;
        window=new Timeline.Window();
        period=new Timeline.Period();
        startTimeMs=SystemClock.elapsedRealtime();
    }

    private String getSessionTimeString() {
        return getTimeString(SystemClock.elapsedRealtime() - startTimeMs);
    }

    private static String getTimeString(long timeMs) {
        return timeMs == C.TIME_UNSET ? "?" : TIME_FORMAT.format((timeMs) / 1000f);
    }
    private static String getStateString(int state) {
        switch (state) {
            case ExoPlayer.STATE_BUFFERING:
                return "B";
            case ExoPlayer.STATE_ENDED:
                return "E";
            case ExoPlayer.STATE_IDLE:
                return "I";
            case ExoPlayer.STATE_READY:
                return "R";
            default:
                return "?";
        }
    }
    // Exoplayer eventlistener

    @Override
    //Called when the timeline or the manifest has been refreshed
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        int periodCount=timeline.getPeriodCount();
        int windowCount=timeline.getWindowCount();
        Log.d(TAG, "sourceInfo ["+getSessionTimeString()+"periodCount=" + periodCount + ", windowCount=" + windowCount);
        for (int i = 0; i < periodCount; i++) {
            timeline.getPeriod(i, period);
            Log.d(TAG, "  " +  "period [" + getTimeString(period.getDurationMs()) + "]");
        }
        for (int i = 0; i < windowCount; i++) {
            timeline.getWindow(i, window);
            Log.d(TAG, "  " +  "window [" + getTimeString(window.getDurationMs()) + ", "
                    + window.isSeekable + ", " + window.isDynamic + "]");
        }

    }

    @Override
    //Called when the available or selected tracks change
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo == null) {
            Log.d(TAG, "Tracks []");
            return;
        }
        Log.d(TAG, "Tracks [");
        for(int rendererIndex=0;rendererIndex< mappedTrackInfo.length;rendererIndex++){
            TrackGroupArray rendererTrackGroups=mappedTrackInfo.getTrackGroups(rendererIndex);
            TrackSelection trackSelection=trackSelections.get(rendererIndex);
            if(rendererTrackGroups.length>0){
                Log.d(TAG, "  Renderer:" + rendererIndex + " [");
                for (int groupIndex=0;groupIndex< rendererTrackGroups.length;groupIndex++){
                    TrackGroup trackGroup=rendererTrackGroups.get(groupIndex);
                    String adaptiveSupport = getAdaptiveSupportString(trackGroup.length,
                            mappedTrackInfo.getAdaptiveSupport(rendererIndex, groupIndex, false));
                    Log.d(TAG, "    Group:" + groupIndex + ", adaptive_supported=" + adaptiveSupport + " [");
                    for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
                        String status = getTrackStatusString(trackSelection, trackGroup, trackIndex);
                        String formatSupport = getFormatSupportString(
                                mappedTrackInfo.getTrackFormatSupport(rendererIndex, groupIndex, trackIndex));
                        Log.d(TAG, "      " + status + " Track:" + trackIndex + ", "
                                + Format.toLogString(trackGroup.getFormat(trackIndex))
                                + ", supported=" + formatSupport);
                    }
                    Log.d(TAG, "    ]");
                }

                if(trackSelection!=null){
                    for (int selectionIndex = 0; selectionIndex < trackSelection.length(); selectionIndex++) {
                        Metadata metadata = trackSelection.getFormat(selectionIndex).metadata;
                        if (metadata != null) {
                            Log.d(TAG, "    Metadata [");
                            printMetadata(metadata, "      ");
                            Log.d(TAG, "    ]");
                            break;
                        }
                    }
                }
                Log.d(TAG, "  ]");
            }
        }

        TrackGroupArray unassocatedTrackGroups=mappedTrackInfo.getUnassociatedTrackGroups();
        if(unassocatedTrackGroups.length>0){
            Log.d(TAG, "  Renderer:None [");
            for (int groupIndex=0;groupIndex<unassocatedTrackGroups.length;groupIndex++){
                Log.d(TAG, "    Group:" + groupIndex + " [");
                TrackGroup trackGroup=unassocatedTrackGroups.get(groupIndex);
                for(int trackIndex=0;trackIndex<trackGroup.length;trackIndex++){
                    String status = getTrackStatusString(false);
                    String formatSupport = getFormatSupportString(
                            RendererCapabilities.FORMAT_UNSUPPORTED_TYPE);
                    Log.d(TAG, "      " + status + " Track:" + trackIndex + ", "
                            + Format.toLogString(trackGroup.getFormat(trackIndex))
                            + ", supported=" + formatSupport);
                }
                Log.d(TAG, "    ]");

            }
            Log.d(TAG, "  ]");

        }
        Log.d(TAG, "]");

    }

    @Override
    // Called when the player starts or stops loading the source
    public void onLoadingChanged(boolean isLoading) {
        Log.d(TAG,"loading ["+isLoading+"]");
    }

    @Override
    // called playwhenready or playbackState changes
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.d(TAG, "state [" + getSessionTimeString() + ", " + playWhenReady + ", "
                + getStateString(playbackState) + "]");
    }

    @Override
    //Called when an error occurs. The playback state transition to STATE_IDLE
    public void onPlayerError(ExoPlaybackException error) {
        Log.e(TAG, "playerFailed [" + getSessionTimeString() + "]", error);
    }

    @Override
    //current window or period index changes.
    public void onPositionDiscontinuity() {
        //Log.d(TAG, "positionDiscontinuity");
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        //Log.d(TAG, "playbackParameters " + String.format(
        //        "[speed=%.2f, pitch=%.2f]", playbackParameters.speed, playbackParameters.pitch));
    }


    // AudioRendererEventlistener

    @Override
    // Called when the renderer is enabled
    public void onAudioEnabled(DecoderCounters counters) {
        //Log.d(TAG, "audioEnabled [" + getSessionTimeString() + "]");
    }

    @Override
    // Called when audosession id is set
    public void onAudioSessionId(int audioSessionId) {
        //Log.d(TAG, "audioSessionId [" + audioSessionId + "]");
    }

    @Override
    // called when a decoder is created
    public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
        //Log.d(TAG, "audioDecoderInitialized [" + getSessionTimeString() + ", " + decoderName + "]");
    }

    @Override
    // called when the format of the media being consumed by renderer changes
    public void onAudioInputFormatChanged(Format format) {
       // Log.d(TAG, "audioFormatChanged [" + getSessionTimeString() + ", " + Format.toLogString(format)
       //         + "]");
    }

    @Override

    public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
        //printInternalError("audioTrackUnderrun [" + bufferSize + ", " + bufferSizeMs + ", "
         //       + elapsedSinceLastFeedMs + "]", null);
    }

    @Override
    // called when the renderer is disabled
    public void onAudioDisabled(DecoderCounters counters) {
        //Log.d(TAG, "audioDisabled [" + getSessionTimeString() + "]");
    }


    //MetadataRenderer.Output

    @Override
    public void onMetadata(Metadata metadata) {
       // Log.d(TAG, "onMetadata [");
       // printMetadata(metadata, "  ");
       //Log.d(TAG, "]");
    }

    // AdaptiveMediaSourceEventListener

    @Override
    public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs) {
        Log.d(TAG,"STREAMLET["+getSessionTimeString()+"start time"+ Long.toString(mediaStartTimeMs));
    }

    @Override
    public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {

    }

    @Override
    public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {

    }

    @Override
    public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded, IOException error, boolean wasCanceled) {
        printInternalError("loadError", error);
    }

    @Override
    public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) {

    }

    @Override
    public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaTimeMs) {

    }

    // ExtractorMediaSource.EventListener

    @Override
    public void onLoadError(IOException error) {
        printInternalError("loadError", error);
    }

    // VideoRendererEventListener

    @Override
    // called when a renderer is enabled.
    public void onVideoEnabled(DecoderCounters counters) {
        //Log.d(TAG, "videoEnabled [" + getSessionTimeString() + "]");
    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
        Log.d(TAG, "videoDecoderInitialized [" + getSessionTimeString() + ", " + decoderName + "]");
    }

    @Override
    public void onVideoInputFormatChanged(Format format) {
        Log.d(TAG, "videoFormatChanged [" + getSessionTimeString() + ", " + Format.toLogString(format)
                + "]");

    }

    @Override
    // Called to report the number of frames dropped by renderer
    public void onDroppedFrames(int count, long elapsedMs) {
        //Log.d(TAG, "droppedFrames [" + getSessionTimeString() + ", " + count + "]");
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        //Log.d(TAG, "videoSizeChanged [" + width + ", " + height + "]");
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {
        //Log.d(TAG, "renderedFirstFrame [" + surface + "]");
    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {
       // Log.d(TAG, "videoDisabled [" + getSessionTimeString() + "]");
    }


    private void printInternalError(String type, Exception e) {
        Log.e(TAG, "internalError [" + getSessionTimeString() + ", " + type + "]", e);
    }

    private void printMetadata(Metadata metadata, String prefix) {
        for (int i = 0; i < metadata.length(); i++) {
            Metadata.Entry entry = metadata.get(i);
            if (entry instanceof TextInformationFrame) {
                TextInformationFrame textInformationFrame = (TextInformationFrame) entry;
                Log.d(TAG, prefix + String.format("%s: value=%s", textInformationFrame.id,
                        textInformationFrame.value));
            } else if (entry instanceof UrlLinkFrame) {
                UrlLinkFrame urlLinkFrame = (UrlLinkFrame) entry;
                Log.d(TAG, prefix + String.format("%s: url=%s", urlLinkFrame.id, urlLinkFrame.url));
            } else if (entry instanceof PrivFrame) {
                PrivFrame privFrame = (PrivFrame) entry;
                Log.d(TAG, prefix + String.format("%s: owner=%s", privFrame.id, privFrame.owner));
            } else if (entry instanceof GeobFrame) {
                GeobFrame geobFrame = (GeobFrame) entry;
                Log.d(TAG, prefix + String.format("%s: mimeType=%s, filename=%s, description=%s",
                        geobFrame.id, geobFrame.mimeType, geobFrame.filename, geobFrame.description));
            } else if (entry instanceof ApicFrame) {
                ApicFrame apicFrame = (ApicFrame) entry;
                Log.d(TAG, prefix + String.format("%s: mimeType=%s, description=%s",
                        apicFrame.id, apicFrame.mimeType, apicFrame.description));
            } else if (entry instanceof CommentFrame) {
                CommentFrame commentFrame = (CommentFrame) entry;
                Log.d(TAG, prefix + String.format("%s: language=%s, description=%s", commentFrame.id,
                        commentFrame.language, commentFrame.description));
            } else if (entry instanceof Id3Frame) {
                Id3Frame id3Frame = (Id3Frame) entry;
                Log.d(TAG, prefix + String.format("%s", id3Frame.id));
            } else if (entry instanceof EventMessage) {
                EventMessage eventMessage = (EventMessage) entry;
                Log.d(TAG, prefix + String.format("EMSG: scheme=%s, id=%d, value=%s",
                        eventMessage.schemeIdUri, eventMessage.id, eventMessage.value));
            }
        }
    }

    private static String getFormatSupportString(int formatSupport) {
        switch (formatSupport) {
            case RendererCapabilities.FORMAT_HANDLED:
                return "YES";
            case RendererCapabilities.FORMAT_EXCEEDS_CAPABILITIES:
                return "NO_EXCEEDS_CAPABILITIES";
            case RendererCapabilities.FORMAT_UNSUPPORTED_SUBTYPE:
                return "NO_UNSUPPORTED_TYPE";
            case RendererCapabilities.FORMAT_UNSUPPORTED_TYPE:
                return "NO";
            default:
                return "?";
        }
    }

    private static String getAdaptiveSupportString(int trackCount, int adaptiveSupport) {
        if (trackCount < 2) {
            return "N/A";
        }
        switch (adaptiveSupport) {
            case RendererCapabilities.ADAPTIVE_SEAMLESS:
                return "YES";
            case RendererCapabilities.ADAPTIVE_NOT_SEAMLESS:
                return "YES_NOT_SEAMLESS";
            case RendererCapabilities.ADAPTIVE_NOT_SUPPORTED:
                return "NO";
            default:
                return "?";
        }
    }

    private static String getTrackStatusString(TrackSelection selection, TrackGroup group,
                                               int trackIndex) {
        return getTrackStatusString(selection != null && selection.getTrackGroup() == group
                && selection.indexOf(trackIndex) != C.INDEX_UNSET);
    }

    private static String getTrackStatusString(boolean enabled) {
        return enabled ? "[X]" : "[ ]";
    }

}
