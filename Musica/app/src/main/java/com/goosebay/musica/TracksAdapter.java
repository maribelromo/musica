package com.goosebay.musica;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by maribel on 2016-12-29.
 */

public class TracksAdapter extends ArrayAdapter<Track> {
    public TracksAdapter(Context context, ArrayList<Track> tracks) {
        super(context, 0, tracks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Track track = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_track, parent, false);
        }

        // Set track name
        TextView trackNameView = (TextView) convertView.findViewById(R.id.trackName);
        trackNameView.setText(track.name);

        // Set artist name
        TextView artistNameView = (TextView) convertView.findViewById(R.id.artistName);
        List<ArtistSimple> artists = track.artists;

        if (artists.size() > 0) {
            artistNameView.setText(artists.get(0).name);
        }

        ImageButton addButton = (ImageButton) convertView.findViewById(R.id.addButton);
        addButton.setTag(position);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (Integer)view.getTag();
                SpotifyManager.getInstance().addToSavedTracks(getItem(position), new SpotifyManager.CompleteListener<Object>() {
                    public void onComplete(Object o){
                        Toast.makeText(getContext(),"Track was added.",Toast.LENGTH_SHORT).show();
                    }
                    public void onError(Throwable error){
                        Toast.makeText(getContext(),"Unable to add track.",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return convertView;
    }
}
