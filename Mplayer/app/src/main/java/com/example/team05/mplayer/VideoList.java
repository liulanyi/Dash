package com.example.team05.mplayer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class VideoList extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG="VideoList";
    private ListView listView;
    private VideoListAdapter adapter;
    private List<VideoInfo> playlist;
    private String rootURL = "http://monterosa.d2.comp.nus.edu.sg/~team05/video-info-DASH.php";
    //private String rootURL = "http://192.168.1.205/androidserver/index.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        listView = (ListView)findViewById(R.id.listview);
        playlist=new ArrayList<>();
        adapter=new VideoListAdapter(this,playlist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(VideoList.this);
        new RetrievePlaylist().execute(rootURL);

        Button btnLoadMore = new Button(this);
        btnLoadMore.setText(getString(R.string.btn_load_more));
        btnLoadMore.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.i(TAG,"Button clicked");
                new RetrievePlaylist().execute(rootURL);
            }
        });
        listView.addFooterView(btnLoadMore);
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.i(TAG,"on Start");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG,"on Resume");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG,"on Pause");
    }
    @Override
    public void onStop(){
        super.onStop();
        Log.i(TAG,"on Stop");
    }

    @Override
    public void onRestart(){
        super.onRestart();
        Log.i(TAG,"on Restart");
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(VideoList.this,MediaPlayer.class);
        Log.i("Click",""+position);
        intent.putExtra("uri",playlist.get(position).getUrl());
        intent.putExtra("title",playlist.get(position).getTitle());
        startActivity(intent);
    }

    //AsyncTask to update listview
    private class RetrievePlaylist extends AsyncTask<String,Integer,String> {

        private static final String REQUEST_METHOD = "GET";
        private static final int READ_TIMEOUT = 15000;
        private static final int CONNECTION_TIMEOUT = 15000;

        protected String doInBackground(String ... in) {
            Log.i(TAG,"Inside asyncTask");
            String stringUrl = in[0];
            String result;
            String inputLine;
            try {
                URL myUrl = new URL(stringUrl);
                HttpURLConnection connection =(HttpURLConnection) myUrl.openConnection();

                //Set methods and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.connect();

                //Create a new InputStreamReader
                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();

                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }

                reader.close();
                streamReader.close();
                connection.disconnect();
                result = stringBuilder.toString();
            }catch(IOException e){
                e.printStackTrace();
                result = "error";
            }

            //parse the JsonObject
            try{
                JSONArray jsonarray=new JSONArray(result);
                for(int i=0;i<jsonarray.length();i++){
                    JSONObject jObject=jsonarray.getJSONObject(i);
                    VideoInfo videoMeta=new VideoInfo(jObject.getString("title"),
                            jObject.getString("description"),
                            jObject.getString("url_DASH"));

                    playlist.add(videoMeta);
                    Log.i(TAG,""+playlist.size());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return result;
        }

        protected void onProgressUpdate(Integer... progress) {
            ProgressDialog pDialog = new ProgressDialog(VideoList.this);
            pDialog.setMessage("Please wait..");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected void onPostExecute(String result) {
            Log.i(TAG,"Downloaded "+result);
            int currentPosition = listView.getFirstVisiblePosition();
            listView.setSelectionFromTop(currentPosition + 1, 0);
        }
    }




}
