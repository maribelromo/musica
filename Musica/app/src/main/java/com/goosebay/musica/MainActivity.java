package com.goosebay.musica;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

import static com.spotify.sdk.android.authentication.AuthenticationResponse.Type.TOKEN;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Request codes to launch activities
    private static final int REQUEST_CODE_LOGIN = 1;

    // API member variables
    private static final String CLIENT_ID = "e6b371e3392c4e8ab74e6c45d4473a40";
    private static final String REDIRECT_URI = "goosebay-musica://oauth";

    private TracksAdapter mTracksAdapter;

    // UI member variables
    private ListView mTrackList;
    private TextView mNoContentView;
    private EditText mSearchEditText;

    private boolean mItemLongPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ensure user is authenticated
        String token = SharedPreferencesManager.getToken(this);
        if (token == null) {
            authenticateUser();
        } else {
            initialize(token);
        }
    }

    private void initialize(String token){
        SpotifyManager.getInstance().init(token);

        // Initialize UI
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mSearchEditText = (EditText)findViewById(R.id.searchTerm);
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    search(mSearchEditText.getText().toString());
                }
                return false;
            }
        });

        mNoContentView = (TextView) findViewById(R.id.noContentView);

        mTracksAdapter = new TracksAdapter(this, new ArrayList<Track>());
        mTrackList = (ListView) findViewById(R.id.tracksList);
        mTrackList.setAdapter(mTracksAdapter);

        handleIntent(getIntent());
    }

    @Override
    public void onNewIntent (Intent intent){
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
            String searchTerm = SearchUtils.getSearchTermFromSubject(subject);

            mSearchEditText.setText(searchTerm);

            search(searchTerm);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE_LOGIN) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                case TOKEN:
                    Toast.makeText(this, "Successful login!", Toast.LENGTH_LONG).show();

                    String accessToken = response.getAccessToken();
                    int expirationLength = response.getExpiresIn();

                    // Store the access token for future app launches
                    SharedPreferencesManager.setAccessToken(this, accessToken , expirationLength);

                    // Initialize
                    initialize(accessToken);
                case ERROR:
                    Log.e(TAG, "Error login in: " + response.getError());
                    break;
                default:
                    Log.e(TAG, "Unknown error: " + response.getType());
                    break;
            }
        }
    }

    private void authenticateUser(){
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "user-library-modify"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE_LOGIN, request);
    }

    private void search(final String searchTerm){

        SpotifyManager.getInstance().search(searchTerm, new SpotifyManager.CompleteListener<List<Track>>() {
            public void onComplete(List<Track> tracks){

                mTracksAdapter.clear();

                if (tracks.size() == 0){
                    mTrackList.setVisibility(View.INVISIBLE);
                    mNoContentView.setVisibility(View.VISIBLE);
                } else {
                    mTrackList.setVisibility(View.VISIBLE);
                    mNoContentView.setVisibility(View.GONE);
                    mTracksAdapter.addAll(tracks);
                }

                mTracksAdapter.notifyDataSetChanged();
            }
            public void onError(Throwable error){
            }
        });
    }
}
