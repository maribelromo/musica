package com.goosebay.musica;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import static com.goosebay.musica.SpotifyApiManager.CLIENT_ID;
import static com.goosebay.musica.SpotifyApiManager.REDIRECT_URI;
import static com.spotify.sdk.android.authentication.AuthenticationResponse.Type.TOKEN;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    // Request codes to launch activities
    private static final int REQUEST_CODE_LOGIN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialize();
    }

    @Override
    public void onNewIntent (Intent intent){
        // Update the intent
        setIntent(intent);
    }

    private void initialize(){
        // If the user is authenticated take them to search, otherwise as them to log in.
        String token = SharedPreferencesManager.getToken(this);
        if (token == null) {
            // If this is the first time the user logs in show them the login screen.
            if (SharedPreferencesManager.isFirstLogin(this)) {
                setContentView(R.layout.activity_login);
            } else {
                // Otherwise try to log them in automatically.
                authenticateUser();
            }
        } else {
            launchSearchActivity(token);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE_LOGIN) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                case TOKEN:
                    Log.d(TAG, "Log in was successful");

                    // Store the the user has successfully logged in.
                    SharedPreferencesManager.setFirstLogin(this, false);

                    String accessToken = response.getAccessToken();
                    int expirationLength = response.getExpiresIn();

                    // Store the access token for future app launches
                    SharedPreferencesManager.setAccessToken(this, accessToken , expirationLength);

                    // Launch the search activity
                    launchSearchActivity(accessToken);
                    break;

                case ERROR:
                    Log.e(TAG,"Log in error: " + response.getError());
                    setContentView(R.layout.activity_login);
                    Toast.makeText(this, "An error occurred, please try again.", Toast.LENGTH_SHORT).show();
                default:
                    Log.e(TAG,"Response type: " + response.getType());
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

    private void launchSearchActivity(String token) {
        Intent intent = getIntent();

        String action = intent.getAction();
        String type = intent.getType();

        String searchTerm = null;
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Bundle extras = intent.getExtras();

            if (extras == null)
                return;

            searchTerm = extras.getString(Intent.EXTRA_SUBJECT);
        }

        Intent newIntent = new Intent(this, SearchActivity.class);

        if (token != null) {
            newIntent.putExtra(SearchActivity.INTENT_EXTRA_TOKEN, token);
        }

        if (searchTerm != null) {
            newIntent.putExtra(SearchActivity.INTENT_SEARCH_TERM, searchTerm);
        }

        startActivity(newIntent);
        finish();
    }

    public void loginClicked(View view) {
        authenticateUser();
    }
}
