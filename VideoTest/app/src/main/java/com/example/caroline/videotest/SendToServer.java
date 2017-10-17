package com.example.caroline.videotest;

/**
 * Created by caroline on 17/10/2017.
 */
public class SendToServer {

    private String title;
    private String description ;

    private String filePath;

    public SendToServer(String title, String description){
        this.title=title;
        this.description=description;
    }

    public SendToServer(String filePath){
        this.filePath = filePath;
    }

}
