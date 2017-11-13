package com.example.caroline.videotest;

import android.util.Log;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by caroline on 20/10/2017.
 */
public class SegmentVideos {

    private double duration ;
    private int numberOfSmallVideos;


    public void segmentVideo(){
        double startTime = 0 ;
        double splitDuration = 3.0 ;
        try {
            //duration = getDuration(tracks.get(0)) / tracks.get(0).getTrackMetaData().getTimescale();

            // duration in seconds
            duration = getDuration(Variables.getFilePath());
            numberOfSmallVideos = (int) (duration / 3);
            System.out.println("DDDDDDDDDDD");
            System.out.println(duration);
            System.out.println(numberOfSmallVideos);

            ArrayList<String> listNameOf3sVideos = new ArrayList<String>();

            for (int num = 0; num < numberOfSmallVideos; num++) {
                System.out.println("START and END " + startTime + " " + (startTime + splitDuration));
                if (startTime == 0){
                    cutVideo3sec(startTime, startTime+splitDuration, num, listNameOf3sVideos);
                    startTime += splitDuration;
                }
                else {
                    cutVideo3sec(startTime+1, startTime+splitDuration, num, listNameOf3sVideos);
                    startTime += splitDuration;
                }


            }
            // last part of the video, which duration is less than 3 sec
            cutVideo3sec(startTime+1, duration, numberOfSmallVideos, listNameOf3sVideos);

            Variables.setListFilePath(listNameOf3sVideos);
            System.out.println("LISTTTTTT");
            System.out.println(listNameOf3sVideos);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected static double getDuration(String filename) {
        try{
            IsoFile isoFile = new IsoFile(filename);
            double lengthInSeconds = (double)
                    isoFile.getMovieBox().getMovieHeaderBox().getDuration() / isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
            System.err.println(lengthInSeconds);
            return lengthInSeconds;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];

            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                // samples always start with 1 but we start with zero therefore +1
                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;

        }
        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample >= cutHere) {
                if (next) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }

    private void cutVideo3sec (double startTime, double endTime, int i, ArrayList<String> listNameOf3sVideos){
        try{

            Movie movie = MovieCreator.build(Variables.getFilePath());
            List<Track> tracks = movie.getTracks();
            //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            //System.out.println(tracks);
            movie.setTracks(new LinkedList<Track>());
            //Movie movie1 = new Movie();
            System.out.println("IIIIIIIIIII");
            System.out.println(i);


            boolean timeCorrected = false;

            // Here we try to find a track that has sync samples. Since we can only start decoding
            // at such a sample we SHOULD make sure that the start of the new fragment is exactly
            // such a frame
            for (Track track : tracks) {
                if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                    if (timeCorrected) {
                        // This exception here could be a false positive in case we have multiple tracks
                        // with sync samples at exactly the same positions. E.g. a single movie containing
                        // multiple qualities of the same video (Microsoft Smooth Streaming file)

                        throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                    }
                    startTime = correctTimeToSyncSample(track, startTime, false);
                    endTime = correctTimeToSyncSample(track, endTime, true);
                    timeCorrected = true;

                }
            }

            for (Track track : tracks) {
                long currentSample =  0;
                double currentTime = 0 ;
                double lastTime = 0;
                long startSample = 0;
                long endSample = 0;

                for (int j = 0; j < track.getSampleDurations().length; j++) {
                    long delta = track.getSampleDurations()[j];
                    if (currentTime <= startTime) { //currentTime > lastTime &&
                        // current sample is still before the new starttime
                        startSample = currentSample;
                    }
                    if (currentTime <= endTime) { //currentTime > lastTime &&
                        // current sample is after the new start time and still before the new endtime
                        endSample = currentSample;
                    } else {
                        // current sample is after the end of the cropped video
                        break;
                    }
                    currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
                    currentSample++;
                }
                Log.i("DASH", "Start time = " + startTime + ", End time = " + endTime);
                movie.addTrack(new CroppedTrack(track, startSample, endSample));
                //movie.addTrack(new CroppedTrack(track, (long) startTime, (long) endTime));
            }
            Container out = new DefaultMp4Builder().build(movie);
            String filename = Variables.getFilePathWithoutExt()+"_"+ String.valueOf(i+1)+".mp4";
            FileOutputStream fos = new FileOutputStream(filename);
            System.out.println("FFFFFFFFFFFFFFFF");
            System.out.println(filename);

            listNameOf3sVideos.add(filename);

            FileChannel fc = fos.getChannel();
            out.writeContainer(fc);
            fc.close();
            fos.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
