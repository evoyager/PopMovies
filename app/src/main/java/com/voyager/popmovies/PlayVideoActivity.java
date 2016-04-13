package com.voyager.popmovies;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

public class PlayVideoActivity extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener {

    private static final int RECOVERY_DIALOG_REQUEST = 10;
    public static final String API_KEY = BuildConfig.YOUTUBE_API_KEY;

    //From URL -> https://www.youtube.com/watch?v=kHue-HaXXzg
    // Let It Go : "Frozen"
    private String VIDEO_ID ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playerview_demo);

        Bundle extras = getIntent().getExtras();
        VIDEO_ID = extras.getString("key");
        YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubeView.initialize(API_KEY, this);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format("YouTube Error (%1$s)",
                    errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onInitializationSuccess(Provider provider,
                                        YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {
            player.cueVideo(VIDEO_ID);
        }
        player.setFullscreen(true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(API_KEY, this);
        }
    }

    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerView) findViewById(R.id.youtube_view);
    }

}