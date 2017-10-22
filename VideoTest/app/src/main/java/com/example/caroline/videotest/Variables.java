package com.example.caroline.videotest;

import java.util.ArrayList;

/**
 * Created by caroline on 17/10/2017.
 */
public class Variables {

    private static String title ;
    private static String description ;

    private static String filePath;
    private static ArrayList<String> listFilePath ;
    private static String workingPath;
    private static String filePathWithoutExt;

    // Getter and Setter de title
    public static String getTitle(){return title;}
    public static void setTitle(String title){Variables.title = title;}

    // Getter and Setter de description
    public static String getDescription(){return description;}
    public static void setDescription(String description){Variables.description = description;}

    // Getter and Setter de filepath
    public static String getFilePath() {return filePath;}
    public static void setFilePath(String filePath) {Variables.filePath = filePath;}

    public static ArrayList<String> getListFilePath() {return listFilePath;}
    public static void setListFilePath(ArrayList<String> listFilePath) {Variables.listFilePath = listFilePath;}

    public static String getWorkingPath(){return workingPath;}
    public static void setWorkingPath(String workingPath){Variables.workingPath = workingPath;}

    public static String getFilePathWithoutExt(){return filePathWithoutExt;}
    public static void setFilePathWithoutExt(String filePathWithoutExt){Variables.filePathWithoutExt = filePathWithoutExt;}

}

