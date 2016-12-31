package com.goosebay.musica;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

import static android.view.MotionEvent.ACTION_CANCEL;

/**
 * Created by maribel on 2016-12-29.
 */

public class TracksAdapter extends ArrayAdapter<Track> {
    // True if we are currently handling a long press
    private boolean mItemLongPressed;

    // Pulse animation object
    private ObjectAnimator mPulseAnimator;

    private HashMap<String,Boolean> mAddedTracksHashMap = new HashMap<String,Boolean>();;

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
        } else {
            artistNameView.setText("");
        }

        // Set the album art
        AlbumSimple album = track.album;
        ImageView albumArtImageView =  (ImageView) convertView.findViewById(R.id.albumArt);

        // Set the default album art
        albumArtImageView.setImageResource(R.drawable.default_album_art);

        if (album != null){
            List<Image> images = album.images;

            if (images != null && images.size() > 0) {
                Image firstAlbumArt = album.images.get(0);
                Picasso.with(getContext()).load(firstAlbumArt.url)
                        .placeholder(R.drawable.default_album_art)
                        .error(R.drawable.default_album_art)
                        .into(albumArtImageView);
            }
        }

        // Add listeners for user interactions
        addInteractionListeners(convertView,position);

        return convertView;
    }

    private void addInteractionListeners(View item, int position){

        LinearLayout trackDetailsContainer = (LinearLayout) item.findViewById(R.id.trackDetailsContainer);
        trackDetailsContainer.setTag(position);

        // Add long press listener to know when the user does a long press
        trackDetailsContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = (Integer)view.getTag();
                mItemLongPressed = true;

                // Play the track
                SpotifyPlayerManager.getInstance().playTrack(getItem(position));

                view.setBackgroundColor(ContextCompat.getColor(getContext(),
                        R.color.listHighlightColor));

                // Show the playing indicator in the item
                showPlayingIndicator(view);
                return true;
            }
        });

        // Add touch listener to know when the long press ends
        trackDetailsContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int position = (Integer)view.getTag();
                // We're only interested in when the button is released.
                if (motionEvent.getAction() == MotionEvent.ACTION_UP
                        || motionEvent.getAction() == ACTION_CANCEL) {
                    // We're only interested in anything if our speak button is currently pressed.
                    if (mItemLongPressed) {
                        // Do something when the button is released.
                        mItemLongPressed = false;

                        view.setBackgroundColor(ContextCompat.getColor(getContext(),
                                R.color.appBackground));

                        hidePlayingIndicator(view);
                        SpotifyPlayerManager.getInstance().stopPlayback();
                    }
                }

                // Need to return false otherwise we won't we can get long click events
                return false;
            }
        });

        ImageButton addButton = (ImageButton) item.findViewById(R.id.addButton);
        addButton.setTag(position);

        // Add click listener for the add button
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                // Get the track for this row
                int position = (Integer)view.getTag();
                final Track track = getItem(position);

                // If the track has already been added, remove it
                if (mAddedTracksHashMap.containsKey(track.id)){
                    // Remove this track from the user's saved tracks

                    SpotifyDataManager.getInstance().removeFromSavedTracks(track,
                            new SpotifyDataManager.CompleteListener<Object>() {
                                public void onComplete(Object o) {
                                    mAddedTracksHashMap.remove(track.id);

                                    AnimationUtils.rotate45Degrees(view);

                                    Toast.makeText(getContext(), "Track was removed.",
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }

                                public void onError(Throwable error) {
                                    Toast.makeText(getContext(), "Unable to remove track.",
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
                } else {
                    // Add this track to the user's saved tracks
                    SpotifyDataManager.getInstance().addToSavedTracks(track,
                            new SpotifyDataManager.CompleteListener<Object>() {
                                public void onComplete(Object o) {
                                    mAddedTracksHashMap.put(track.id, true);

                                    AnimationUtils.rotate45Degrees(view);

                                    Toast.makeText(getContext(), "Track was added.",
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }

                                public void onError(Throwable error) {
                                    Toast.makeText(getContext(), "Unable to add track.",
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
                }
            }
        });
    }



    private void showPlayingIndicator(View view) {
        ImageButton addButton = (ImageButton) view.findViewById(R.id.addButton);

        // Show the playing icon
        addButton.setImageResource(R.drawable.ic_volume_up_white_36dp);

        mPulseAnimator = AnimationUtils.getPulseAnimation(addButton);
        mPulseAnimator.start();
    }

    private void hidePlayingIndicator(View view) {
        // Cancel pulse animation
        mPulseAnimator.cancel();

        // Set the image of the button back to the add icon
        ImageButton addButton = (ImageButton) view.findViewById(R.id.addButton);
        addButton.setImageResource(R.drawable.ic_add_white_36dp);
    }
}
