package com.goosebay.musica;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

import static com.goosebay.musica.R.id.searchTerm;

public class SearchActivity extends AppCompatActivity {

    static final String INTENT_EXTRA_TOKEN = "INTENT_EXTRA_TOKEN";
    static final String INTENT_SEARCH_TERM = "INTENT_SEARCH_TERM";

    private TracksAdapter mTracksAdapter;
    private SpotifyTrackPlayer mPlayer;

    // UI member variables
    private ListView mTrackList;
    private TextView mNoContentView;
    private EditText mSearchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        String token = intent.getStringExtra(INTENT_EXTRA_TOKEN);

        mPlayer = new SpotifyTrackPlayer(token, this);
        SpotifyApiManager.getInstance().init(token);

        initializeUI();

        initiateSearch(intent);
    }

    public void onDestroy(){
        mPlayer.cleanUp();
        super.onDestroy();
    }

    public void onNewIntent (Intent intent){
        initiateSearch(intent);
    }

    private void initiateSearch (Intent intent){
        String subject = intent.getStringExtra(INTENT_SEARCH_TERM);
        if (subject != null){
            String searchTerm = SearchUtils.getSearchTermFromSubject(subject);

            mSearchEditText.setText(searchTerm);
            mSearchEditText.setSelection(searchTerm.length());

            search(searchTerm);
        }
    }

    private void initializeUI(){
        // Initialize UI
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSearchEditText = (EditText)findViewById(searchTerm);

        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    search(mSearchEditText.getText().toString());
                }
                return false;
            }
        });

        mSearchEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP
                        && mSearchEditText.getCompoundDrawables()[2] != null) {

                    int textEndX = mSearchEditText.getWidth() - mSearchEditText.getPaddingRight()
                            - mSearchEditText.getCompoundDrawablePadding()
                            - mSearchEditText.getCompoundDrawables()[2].getIntrinsicWidth();

                    // The clear button was clicked, clear the text
                    if (event.getX() > textEndX) {
                        mSearchEditText.setText("");
                    }
                }
                return false;
            }
        });

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isEmpty = mSearchEditText.getText().toString().equals("");

                mSearchEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                        isEmpty ? 0 : R.drawable.ic_clear_white_24dp, 0);
            }

            @Override
            public void afterTextChanged(Editable arg0) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });


        mNoContentView = (TextView) findViewById(R.id.noContentView);

        mTracksAdapter = new TracksAdapter(this, new ArrayList<Track>(), mPlayer);
        mTrackList = (ListView) findViewById(R.id.tracksList);
        mTrackList.setAdapter(mTracksAdapter);
    }

    private void search(final String searchTerm){
        SpotifyApiManager.getInstance().search(searchTerm, new SpotifyApiManager.CompleteListener<List<Track>>() {
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
