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
public class SpotifyManager {

    private SpotifyApi mApi = null;
    private SpotifyService mSpotify = null;
    private static SpotifyManager mInstance = null;

    public interface CompleteListener<T> {
        void onComplete(T item);
        void onError(Throwable error);
    }

    public static SpotifyManager getInstance() {
        if (mInstance == null){
            mInstance = new SpotifyManager();
        }
        return mInstance;
    }

    private SpotifyManager() {}

    public void init (String token){
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
}
