package com.example.team05.mplayer;

/**
 * Created by lanyi on 27/09/2017.
 */

public class VideoInfo {
    private String title;
    private String description;
    private String url;

    public VideoInfo(String _title, String _description, String _url){
        title=_title;
        description=_description;
        url=_url;
    }

    public String getTitle(){
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

}
