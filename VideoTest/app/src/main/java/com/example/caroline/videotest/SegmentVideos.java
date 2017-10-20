package com.example.caroline.videotest;

import java.util.ArrayList;

/**
 * Created by caroline on 20/10/2017.
 */
public class SegmentVideos {

    private long duration ;
    private int numberOfSmallVideos;
    private double startTime;
    private double endTime;
    private String filePath;

    public void setList (String filepath){
        this.filePath = filepath ;
        ArrayList<String> list = new ArrayList<String>();
        list.add(filepath);

        Variables.setListFilePath(list);
    }

}
