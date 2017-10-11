package com.example.team05.mplayer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lanyi on 27/09/2017.
 */

public class VideoListAdapter extends BaseAdapter {
    private static final String TAG="VideoListAdapter";
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<VideoInfo> mEntries;

    public VideoListAdapter(Context mContext,List<VideoInfo> items){

        this.mContext = mContext;
        mLayoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mEntries=items;


    }
    @Override
    public int getCount() {
        return mEntries.size();
    }

    @Override
    public Object getItem(int position) {
        return mEntries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView =  mLayoutInflater.inflate(R.layout.list_item, parent, false);
        // Get title element
        TextView titleView =
                (TextView) rowView.findViewById(R.id.listTitle);

        // Get description element
        TextView descriptionView =
                (TextView) rowView.findViewById(R.id.listDescription);
        Log.i(TAG,""+position);
        VideoInfo item = (VideoInfo) getItem(position);
        titleView.setText(item.getTitle());
        descriptionView.setText(item.getDescription());

        return rowView;
    }
}
