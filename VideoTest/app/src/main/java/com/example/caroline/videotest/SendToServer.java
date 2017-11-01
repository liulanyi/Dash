package com.example.caroline.videotest;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Created by caroline on 17/10/2017.
 */
public class SendToServer extends AsyncTask<String, Void, Void> {

    private String title;
    private String description ;

    private ArrayList<String> listFilePath;
    private ArrayList<String> oldlistFilePath ;
    private String filePath;

    String FromServer;

    private String URLnew = "http://monterosa.d2.comp.nus.edu.sg/~team05/new.php"; // POST, Json (title + descritpion)
    //private String URLnew ="https://perso.telecom-paristech.fr/lperache/new.php"; // TODO change

    /// return number id
    /// get id
    // utiliser l'id pour nommer la video a envoyer
    /// id-partNumber.mp4  (filepath)
    // envoyer
    private String URLupload = "http://monterosa.d2.comp.nus.edu.sg/~team05/upload.php"; //
    //private String URLupload = "https://perso.telecom-paristech.fr/lperache/upload.php"; // TODO change
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
        this.oldlistFilePath = listFilePath;
    }

    @Override
    protected Void doInBackground(String... arg0) {
        // try to connect to the server
        String id = POST(URLnew, title, description);

        int numOfVideos = listFilePath.size();
        System.out.println("NNNNNNNNNNNNNNNNN " + numOfVideos);
        File copy = null ;
        // rename the videos
        for (int i=0; i<numOfVideos; i++){
            //setFileName(listFilePath.get(i), id + "-" + i + ".mp4");
            try {
                Thread.sleep(2000);
                System.out.println("LLLLLLLLLLLLL " + Variables.getListFilePath());
                System.out.println(listFilePath.get(i));
                copy = exportFile(listFilePath.get(i), Variables.getWorkingPath()+ "/" + id + "-" + i + ".mp4");
                System.out.println(copy.getPath());

            }catch (Exception e){
                e.printStackTrace();
            }
            listFilePath.set(i, Variables.getWorkingPath() + "/"+ id + "-" + i + ".mp4");

            String response = POST(URLupload, copy.getPath());
            //String response = POST(URLupload, id + "-" + i + ".mp4");
            System.out.println("RESPONSEEEEEEE" + " : " + response);

        }


        return null;
    }


    private File exportFile(String src, String dst) throws IOException {

        File expFile = new File(dst);
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(expFile).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }

        return expFile;
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

            connectionNew.setRequestProperty("Content-Type", "application/json"); // charset=UTF-8");

            // create jsonObject to send title and description
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("title", title);
            jsonObject.put("description", description);

            System.out.println("JSONNNN : " + jsonObject.toString());

            OutputStream outputStream = connectionNew.getOutputStream();
            outputStream.write(jsonObject.toString().getBytes());
            outputStream.flush();
            outputStream.close();

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

            //connectionNew.disconnect();
            //Set our result equal to our stringBuilder
            id = stringBuilder.toString();
            System.out.println("IIDDDDDD " + id );

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

        //String filePath = Variables.getWorkingPath() + "/"+ fileName ;
        File file = new File(filePath);
        DataOutputStream dos = null;

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        HttpURLConnection connectionUpLoad = null;

        int serverResponseCode;

        /*if (!file.isFile()) {
            Log.e("Huzza", "Source File Does not exist");
            return null;
        }*/

        try{
            FileInputStream inputStream = new FileInputStream(file);

            URL url = new URL(urlString);
            connectionUpLoad = (HttpURLConnection) url.openConnection();
            connectionUpLoad.setRequestMethod("POST");
            connectionUpLoad.setDoOutput(true);
            connectionUpLoad.setDoInput(true);

            connectionUpLoad.setRequestProperty("Connection", "Keep-Alive");
            connectionUpLoad.setRequestProperty("ENCTYPE", "multipart/form-data");
            connectionUpLoad.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connectionUpLoad.setRequestProperty("fileToUpload",filePath);

            //OutputStreamWriter out = new OutputStreamWriter(connectionUpLoad.getOutputStream());
            //out.write(filePath); // TODO Maybe to change

            //OutputStream out = connectionUpLoad.getOutputStream();
            //out.write(file.getBytes());

            dos = new DataOutputStream(connectionUpLoad.getOutputStream());


            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\";filename=\"" + filePath + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = inputStream.available();
            //selecting the buffer size as minimum of available bytes or 1 MB
            bufferSize = Math.min(bytesAvailable,maxBufferSize);
            //setting the buffer as byte array of size of bufferSize
            buffer = new byte[bufferSize];

            //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
            bytesRead = inputStream.read(buffer,0,bufferSize);

            //loop repeats till bytesRead = -1, i.e., no bytes are left to read
            while (bytesRead > 0){
                //write the bytes read from inputstream
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = inputStream.available();
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                bytesRead = inputStream.read(buffer,0,bufferSize);
            }

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            serverResponseCode = connectionUpLoad.getResponseCode();

            inputStream.close();
            dos.flush();
            dos.close();


            if (serverResponseCode==200){
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

                connectionUpLoad.disconnect();


                return result ;
            }
            else {
                return "could not upload";
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(connectionUpLoad != null) // Make sure the connection is not null.
                connectionUpLoad.disconnect();
        }

        return result;
    }
}
