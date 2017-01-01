package com.goosebay.musica;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by maribel on 2016-12-29.
 */
public class SpotifyApiManager {

    // API member variables
    public static final String REDIRECT_URI = "goosebay-musica://oauth";
    public static final String CLIENT_ID = "e6b371e3392c4e8ab74e6c45d4473a40";

    private SpotifyApi mApi = null;
    private SpotifyService mSpotify = null;
    private static SpotifyApiManager mInstance = null;

    public interface CompleteListener<T> {
        void onComplete(T item);
        void onError(Throwable error);
    }

    public static SpotifyApiManager getInstance() {
        if (mInstance == null){
            mInstance = new SpotifyApiManager();
        }
        return mInstance;
    }

    private SpotifyApiManager() {}

    public void init (String token){
        if (token == null)
            return;

        mApi = new SpotifyApi();
        mApi.setAccessToken(token);
        mSpotify = mApi.getService();
    }

    public void search(String searchTerm, final CompleteListener<List<Track>> listener){
        if (mSpotify == null || searchTerm == null){
            return;
        }

        mSpotify.searchTracks(searchTerm, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                if (listener != null)
                    listener.onComplete(tracksPager.tracks.items);
            }

            @Override
            public void failure(RetrofitError error) {
                if (listener != null)
                    listener.onError(error);
            }
        });
    }

    public void addToSavedTracks(Track track, final CompleteListener<Object> listener){
        if (mSpotify == null || track == null){
            return;
        }

        mSpotify.addToMySavedTracks(track.id,new Callback<Object>() {
            @Override
            public void success(Object object, Response response) {
                if (listener != null)
                    listener.onComplete(true);
            }

            @Override
            public void failure(RetrofitError error) {
                if (listener != null)
                    listener.onError(error);
            }
        });
    }

    public void removeFromSavedTracks(Track track, final CompleteListener<Object> listener){
        if (mSpotify == null || track == null){
            return;
        }

        mSpotify.removeFromMySavedTracks(track.id,new Callback<Object>() {
            @Override
            public void success(Object object, Response response) {
                if (listener != null)
                    listener.onComplete(true);
            }

            @Override
            public void failure(RetrofitError error) {
                if (listener != null)
                    listener.onError(error);
            }
        });
    }
}
