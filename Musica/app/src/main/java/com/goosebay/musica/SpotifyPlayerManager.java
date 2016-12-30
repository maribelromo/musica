package com.goosebay.musica;

import android.content.Context;
import android.util.Log;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import kaaes.spotify.webapi.android.models.Track;

import static com.goosebay.musica.SpotifyDataManager.CLIENT_ID;


/**
 * Created by maribel on 2016-12-30.
 */
public class SpotifyPlayerManager {

    private static String TAG_NAME = SpotifyDataManager.class.getSimpleName();

    private static SpotifyPlayerManager mInstance;

    private SpotifyPlayer mPlayer = null;

    public static SpotifyPlayerManager getInstance() {
        if (mInstance == null){
            mInstance = new SpotifyPlayerManager();
        }
        return mInstance;
    }

    public void init (String token, Context context){
        if (token == null || context == null)
            return;

        if (mPlayer == null) {
            Config playerConfig = new Config(context.getApplicationContext(), token, CLIENT_ID);

            mPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer player) {
                    Log.d(TAG_NAME, "Player was initialized.");
                }

                @Override
                public void onError(Throwable error) {
                    Log.d(TAG_NAME, "Player was not initialized. " + error);
                }
            });
        } else {
            mPlayer.login(token);
        }
    }

    public void cleanUp() {
        Spotify.destroyPlayer(this);
        mPlayer = null;
    }

    private SpotifyPlayerManager() {
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
