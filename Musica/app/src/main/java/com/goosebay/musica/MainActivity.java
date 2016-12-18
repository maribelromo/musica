package com.goosebay.musica;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public class MainActivity extends AppCompatActivity {

    // Request codes to launch activities
    private static final int REQUEST_CODE_LOGIN = 1;

    // API member variables
    private static final String CLIENT_ID = "e6b371e3392c4e8ab74e6c45d4473a40";
    private static final String REDIRECT_URI = "goosebay-musica://oauth";

    private SpotifyApi mApi = null;
    private SpotifyService mSpotify = null;

    // UI member variables
    private LinearLayout mSongNameContainer;
    private LinearLayout mArtistContainer;

    private TextView mResultMessageTextView;
    private TextView mSongNameTextView;
    private TextView mArtistTextView;
    private Button mYesButton;

    private EditText mSearchEditText;

    // Search variables
    private String mCurrentSearchTerm = null;
    private Track mCurrentTrack = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Authenticate the user
        authenticateUser();

        // Initialize UI
        initializeUI();
    }

    private void initializeUI(){
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mResultMessageTextView = (TextView) findViewById(R.id.result_msg);

        mSongNameContainer = (LinearLayout) findViewById(R.id.song_container);
        mSongNameTextView = (TextView) findViewById(R.id.song_name);

        mArtistContainer = (LinearLayout) findViewById(R.id.artist_container);
        mArtistTextView = (TextView) findViewById(R.id.artist);

        mYesButton = (Button) findViewById(R.id.yes_button);
        mYesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToSavedTracks(mCurrentTrack);
            }
        });

        mSearchEditText = (EditText)findViewById(R.id.search);
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    search(mSearchEditText.getText().toString());
                }
                return false;
            }
        });
    }

    @Override
    public void onNewIntent (Intent intent){
        // Clear any previous search before handling the new intent
        clearSearch();
        handleIntent(intent);
    }

    public void handleIntent(Intent intent){
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Bundle extras = intent.getExtras();

            if (extras == null)
                return;

            String subject = extras.getString(Intent.EXTRA_SUBJECT);
            mCurrentSearchTerm = getSearchTermFromSubject(subject);
            search(mCurrentSearchTerm);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE_LOGIN) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Toast.makeText(this,"Successful login!", Toast.LENGTH_LONG).show();

                // Initialize the API with the access token to be able to make requests
                mApi = new SpotifyApi();
                mApi.setAccessToken(response.getAccessToken());
                mSpotify = mApi.getService();

                handleIntent(getIntent());
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

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE_LOGIN, request);
    }

    // Subject Example: "Watch "JoJo Siwa - BOOMERANG (Official Video)" on YouTube"
    // TODO: smarter removal using regex, using hardcoded strings for now
    private String getSearchTermFromSubject(String intentSubject){

        intentSubject = intentSubject.toLowerCase();
        intentSubject = intentSubject.replace("\"","");
        intentSubject = intentSubject.replace("watch ","");
        intentSubject = intentSubject.replace(" on youtube","");


        String re = "\\([^()]*\\)";
        Pattern p = Pattern.compile(re);
        Matcher m = p.matcher(intentSubject);
        while (m.find()) {
            intentSubject = m.replaceAll("");
            m = p.matcher(intentSubject);
        }

        re = "\\[[^\\[\\]]*\\]";
        p = Pattern.compile(re);
        m = p.matcher(intentSubject);
        while (m.find()) {
            intentSubject = m.replaceAll("");
            m = p.matcher(intentSubject);
        }

        //intentSubject = intentSubject.replace("(official video)","");
        //intentSubject = intentSubject.replace("[official video]","");

        return intentSubject;
    }

    private void search(String searchTerm){
        if (mSpotify == null || searchTerm == null){
            return;
        }

        mSpotify.searchTracks(searchTerm, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                List<Track> tracks = tracksPager.tracks.items;

                if (tracks.size() > 0) {

                    mResultMessageTextView.setText(R.string.success_msg_tracks_found);
                    mCurrentTrack = tracks.get(0);

                    // Show song name
                    mSongNameContainer.setVisibility(View.VISIBLE);
                    mSongNameTextView.setText(mCurrentTrack.name);

                    List<ArtistSimple> firstTrackArtists = mCurrentTrack.artists;

                    if (firstTrackArtists.size() > 0) {
                        mArtistTextView.setText(firstTrackArtists.get(0).name);
                        mArtistContainer.setVisibility(View.VISIBLE);
                    }

                    mYesButton.setVisibility(View.VISIBLE);
                    mSearchEditText.setVisibility(View.GONE);
                } else {
                    Resources res = getResources();
                    String msg = String.format(res.getString(R.string.error_msg_no_tracks_found),
                            mCurrentSearchTerm);

                    mResultMessageTextView.setText(msg);
                    mSongNameContainer.setVisibility(View.GONE);
                    mArtistContainer.setVisibility(View.GONE);
                    mYesButton.setVisibility(View.GONE);

                    mSearchEditText.setText(mCurrentSearchTerm);
                    mSearchEditText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                mResultMessageTextView.setText(R.string.error_msg_no_tracks_found);
            }
        });
    }

    private void clearSearch(){
        mCurrentTrack = null;
        mCurrentSearchTerm = null;
        mSearchEditText.setText("");
        mSongNameTextView.setText("");
        mArtistTextView.setText("");
        mSongNameContainer.setVisibility(View.GONE);
        mArtistContainer.setVisibility(View.GONE);
        mYesButton.setVisibility(View.GONE);
    }

    private void addToSavedTracks(Track track){
        if (track == null)
            return;

        mSpotify.addToMySavedTracks(track.id,new Callback<Object>() {
            @Override
            public void success(Object object, Response response) {
                mResultMessageTextView.setText(R.string.success_msg_track_added);
                clearSearch();
            }

            @Override
            public void failure(RetrofitError error) {
                mResultMessageTextView.setText(R.string.error_msg_unable_to_add_track);
                clearSearch();
            }
        });
    }
}
