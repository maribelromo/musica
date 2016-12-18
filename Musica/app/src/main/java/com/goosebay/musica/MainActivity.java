package com.goosebay.musica;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public class MainActivity extends AppCompatActivity {

    private static final String TEST_TRACK_ID = "56cswAa9WdFBsjsTyPBAKA";
    private static final String TEST_SEARCH_TERM = "Machine Gun Kelly, Camila Cabello - Bad Things";

    private static final String CLIENT_ID = "e6b371e3392c4e8ab74e6c45d4473a40";
    private static final String REDIRECT_URI = "goosebay-musica://oauth";

    private static final int REQUEST_CODE = 1;

    private SpotifyApi mApi;
    private SpotifyService mSpotify;

    private EditText mSearchEditText;
    private TextView mSongNameTextView;
    private TextView mArtistTextView;
    private Button mAddTrackButton;

    private Track mCurrentTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mSearchEditText = (EditText)findViewById(R.id.search);
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    searchTrack(mSearchEditText.getText().toString());
                }
                return false;
            }
        });

        mSongNameTextView = (TextView) findViewById(R.id.song_name);
        mArtistTextView = (TextView) findViewById(R.id.artist);
        mAddTrackButton = (Button) findViewById(R.id.add_track);

        mAddTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTrack(mCurrentTrack);
            }
        });

        // Authenticate user
        authenticateUser();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Toast.makeText(this,"Successful login!", Toast.LENGTH_LONG).show();

                // Initialize the API with the access token to be able to make requests
                mApi = new SpotifyApi();
                mApi.setAccessToken(response.getAccessToken());
                mSpotify = mApi.getService();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void authenticateUser(){
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "user-library-modify"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    private void searchTrack(String searchTerm){
        if (mSpotify == null || searchTerm == null){
            return;
        }

        mSpotify.searchTracks(searchTerm, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                List<Track> tracks = tracksPager.tracks.items;

                if (tracks.size() > 0) {
                    mCurrentTrack = tracks.get(0);

                    mSongNameTextView.setText(mCurrentTrack.name);

                    List<ArtistSimple> firstTrackArtists = mCurrentTrack.artists;

                    if (firstTrackArtists.size() > 0) {
                        mArtistTextView.setText(firstTrackArtists.get(0).name);
                    }
                } else {
                    Toast.makeText(MainActivity.this,R.string.error_msg_no_tracks_found,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(MainActivity.this, R.string.error_msg_no_tracks_found,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addTrack(Track track){
        if (track == null)
            return;

        mSpotify.addToMySavedTracks(track.id,new Callback<Object>() {
            @Override
            public void success(Object object, Response response) {
                Toast.makeText(MainActivity.this, R.string.success_msg_track_added,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(MainActivity.this, R.string.error_msg_unable_to_add_track,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
