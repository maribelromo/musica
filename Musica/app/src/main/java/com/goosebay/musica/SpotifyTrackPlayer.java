package com.goosebay.musica;

import android.content.Context;
import android.util.Log;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import kaaes.spotify.webapi.android.models.Track;

import static com.goosebay.musica.SpotifyApiManager.CLIENT_ID;


/**
 * Created by maribel on 2016-12-30.
 */
public class SpotifyTrackPlayer {

    private static String TAG_NAME = SpotifyApiManager.class.getSimpleName();

    private SpotifyPlayer mPlayer = null;
    private Context mContext = null;

    public SpotifyTrackPlayer(String token, Context context) {
        if (token == null || context == null)
            return;

        mContext = context;

        Config playerConfig = new Config(mContext.getApplicationContext(), token, CLIENT_ID);

        mPlayer = Spotify.getPlayer(playerConfig, mContext, new com.spotify.sdk.android.player.SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(com.spotify.sdk.android.player.SpotifyPlayer player) {
                Log.d(TAG_NAME, "Player was initialized.");
            }

            @Override
            public void onError(Throwable error) {
                Log.d(TAG_NAME, "Player was not initialized. " + error);
            }
        });
    }

    public void cleanUp() {
        Spotify.destroyPlayer(mContext);
        mPlayer = null;
    }

    public void playTrack(Track track){
        if (mPlayer == null || track == null)
            return;

        mPlayer.playUri(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG_NAME, "Playing track.");
            }
            @Override
            public void onError(Error error) {
                Log.d(TAG_NAME, "Error playing track.");
            }
        }, track.uri, 0, 0);
    }

    public void stopPlayback(){
        if (mPlayer == null)
            return;

        mPlayer.pause(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG_NAME, "Stoppped playing track.");
            }

            @Override
            public void onError(Error error) {
                Log.d(TAG_NAME, "Error pausing playback.");
            }
        });

    }
}
