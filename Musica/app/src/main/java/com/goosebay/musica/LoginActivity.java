package com.goosebay.musica;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import static com.goosebay.musica.SpotifyDataManager.CLIENT_ID;
import static com.goosebay.musica.SpotifyDataManager.REDIRECT_URI;
import static com.spotify.sdk.android.authentication.AuthenticationResponse.Type.TOKEN;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    // Request codes to launch activities
    private static final int REQUEST_CODE_LOGIN = 1;

    private Intent mLastIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialize(getIntent());
    }

    @Override
    public void onNewIntent (Intent intent){
        initialize(intent);
    }

    private void initialize(Intent intent){
        mLastIntent = intent;

        // If the user is authenticated take them to search, otherwise as them to log in.
        String token = SharedPreferencesManager.getToken(this);
        if (token == null) {
            setContentView(R.layout.activity_login);
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
                    Log.d(TAG, "Log in successful: " + response.getError());

                    String accessToken = response.getAccessToken();
                    int expirationLength = response.getExpiresIn();

                    // Store the access token for future app launches
                    SharedPreferencesManager.setAccessToken(this, accessToken , expirationLength);

                    // Initialize
                    launchSearchActivity(accessToken);

                case ERROR:
                    Log.e(TAG, "Log in error: " + response.getError());
                    break;
                default:
                    Log.e(TAG, "Log in unknown error: " + response.getType());
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

    private void launchSearchActivity(String token) {
        String searchTerm = null;

        String action = mLastIntent.getAction();
        String type = mLastIntent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Bundle extras = mLastIntent.getExtras();

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

        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(newIntent);
        finish();
    }

    public void loginClicked(View view) {
        authenticateUser();
    }
}
