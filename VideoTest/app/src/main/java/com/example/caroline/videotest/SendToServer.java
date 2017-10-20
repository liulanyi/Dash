package com.example.caroline.videotest;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by caroline on 17/10/2017.
 */
public class SendToServer extends AsyncTask<String, Void, Void> {

    private String title;
    private String description ;

    private ArrayList<String> listFilePath;
    private String filePath;

    String FromServer;

    private String URLnew = "http://monterosa.d1.comp.nus.edu.sg/~team05/new.php"; // POST, Json (title + descritpion)
    /// return number id
    /// get id
    // utiliser l'id pour nommer la video a envoyer
    /// id-partNumber.mp4  (filepath)
    // envoyer
    private String URLupload = "http://monterosa.d1.comp.nus.edu.sg/~team05/upload.php"; //
    // return text (à pop up)
    // fail : nom existe deja, trop gros (>2M), pas .mp4



    /*public SendToServer(String title, String description, String filePath){ // TODO change with ListFilePath
        this.title=title;
        this.description=description;
        this.filePath = filePath;
    }*/

    public SendToServer(String title, String description, ArrayList<String> listFilePath){
        this.title=title;
        this.description=description;
        this.listFilePath = listFilePath;
    }

    @Override
    protected Void doInBackground(String... arg0) {
        // try to connect to the server
        String id = POST(URLnew, title, description);

        int numOfVideos = listFilePath.size();
        System.out.println("NNNNNNNNNNNNNNNNN " +numOfVideos);
        // rename the videos
        for (int i=0; i<numOfVideos; i++){
            listFilePath.set(i, id + "-" + i + ".mp4");
            System.out.println("LLLLLLLLLLLLL " + Variables.getListFilePath());

            String response = POST(URLupload, listFilePath.get(i));
            System.out.println("RESPONSEEEEEEE " + " : " + response);
        }


        return null;
    }

    public static String POST(String urlString, String title, String description){
        String id = "";
        String inputLine;

        HttpURLConnection connectionNew = null;
        try{
            URL url = new URL(urlString);
            connectionNew = (HttpURLConnection) url.openConnection();
            connectionNew.setRequestMethod("POST");
            connectionNew.setDoOutput(true);

            connectionNew.setRequestProperty("Content-Type", "application/json");

            // create jsonObject to send title and description
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("title", title);
            jsonObject.put("description", description);

            /*OutputStreamWriter out = new OutputStreamWriter(connectionNew.getOutputStream());
            out.write(jsonObject.toString());
            out.flush();
            out.close();*/

            OutputStream outputStream = connectionNew.getOutputStream();
            outputStream.write(jsonObject.toString().getBytes());
            outputStream.flush();

            InputStreamReader in = new InputStreamReader(connectionNew.getInputStream());
            BufferedReader reader = new BufferedReader(in);
            StringBuilder stringBuilder = new StringBuilder();
            //Check if the line we are reading is not null
            while((inputLine = reader.readLine()) != null){
                stringBuilder.append(inputLine);
            }

            //Close our InputStream and Buffered reader
            reader.close();
            in.close();
            //Set our result equal to our stringBuilder
            id = stringBuilder.toString();

            return id ;

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(connectionNew != null) // Make sure the connection is not null.
                connectionNew.disconnect();
        }

        return id;
    }

    public static String POST(String urlString, String filePath){
        String result = "";
        String inputLine;

        HttpURLConnection connectionUpLoad = null;
        try{
            URL url = new URL(urlString);
            connectionUpLoad = (HttpURLConnection) url.openConnection();
            connectionUpLoad.setRequestMethod("POST");
            connectionUpLoad.setDoOutput(true);

            OutputStreamWriter out = new OutputStreamWriter(connectionUpLoad.getOutputStream());
            out.write(filePath); // TODO Maybe to change 
            out.flush();
            out.close();

            InputStreamReader in = new InputStreamReader(connectionUpLoad.getInputStream());
            BufferedReader reader = new BufferedReader(in);
            StringBuilder stringBuilder = new StringBuilder();
            //Check if the line we are reading is not null
            while((inputLine = reader.readLine()) != null){
                stringBuilder.append(inputLine);
            }

            //Close our InputStream and Buffered reader
            reader.close();
            in.close();
            //Set our result equal to our stringBuilder
            result = stringBuilder.toString();

            return result ;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(connectionUpLoad != null) // Make sure the connection is not null.
                connectionUpLoad.disconnect();
        }

        return result;
    }
}
