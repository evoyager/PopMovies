package com.voyager.popmovies;


import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailActivity extends ActionBarActivity {

    private static String trailerLink;
    private static ImageButton btnTrailer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class DetailFragment extends Fragment {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private static final String MOVIE_SHARE_HASHTAG = " #SunshineApp";
        private String mMovieStr;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            btnTrailer = (ImageButton)rootView.findViewById(R.id.btnTrailer);
            btnTrailer.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), PlayVideoActivity.class);
                    intent.putExtra("key", trailerLink);
                    startActivity(intent);
                }
            });


            // The detail Activity called via intent.  Inspect the intent for forecast data.
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                mMovieStr = intent.getStringExtra(Intent.EXTRA_TEXT);
                try {
                    JSONObject movie = new JSONObject(mMovieStr);
                    String title = movie.getString("title");
                    ((TextView) rootView.findViewById(R.id.detail_title))
                            .setText(title);

                    //Poster
                    final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185/";
                    final String poster_key = "poster_path";
                    String poster = POSTER_BASE_URL + movie.getString(poster_key).substring(1);
                    ImageView ivPoster = (ImageView) rootView.findViewById(R.id.imageView);
                    Picasso.with(getActivity()).load(poster).into(ivPoster);

                    //Year
                    String year = movie.getString("release_date").substring(0, 4);;
                    TextView tvYear = (TextView) rootView.findViewById(R.id.tvYear);
                    tvYear.setText(year);

                    //Day/Month
                    String day = movie.getString("release_date").substring(5, 7);;
                    String month = movie.getString("release_date").substring(8);
                    String dayMonth = month + "/" + day;
                    TextView tvDayMonth = (TextView) rootView.findViewById(R.id.tvDayMonth);
                    tvDayMonth.setText(dayMonth);

                    //Overview
                    String overview = movie.getString("overview");
                    TextView tvOverview = (TextView) rootView.findViewById(R.id.tvOverview);
                    tvOverview.setText(overview);

                    //Trailer
                    final String TRAILER_BASE_URL = "http://api.themoviedb.org/3/movie/";
                    String id = movie.getString("id");
                    final String VIDEOS_KEY = "videos";
                    final String KEY_PARAM = "api_key";
                    Uri trailerUri = Uri.parse(TRAILER_BASE_URL).buildUpon()
                            .appendPath(id)
                            .appendPath(VIDEOS_KEY)
                            .appendQueryParameter(KEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                            .build();

                    String trailer = trailerUri.toString();
                    new FetchMoviesTask(rootView).execute(trailer);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return rootView;
        }

        public class FetchMoviesTask extends AsyncTask<String, Void, String> {

            View v;

            FetchMoviesTask(View v) {
                this.v = v;
            }

            String getKeyFromJson(String str) {
                JSONObject trailerJson = null;
                try {
                    trailerJson = new JSONObject(str);
                    final String RESULTS = "results";
                    JSONArray trailerArray = trailerJson.getJSONArray(RESULTS);
                    JSONObject resultJsonObj = trailerArray.getJSONObject(0);
                    String trailerKey = resultJsonObj.getString("key");
                    return trailerKey;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected String doInBackground(String... params) {

                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                String moviesJsonStr = null;
                try {
                URL url = new URL(params[0]);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                moviesJsonStr = buffer.toString();
//                Log.v(LOG_TAG, "Pased JSON: " + moviesJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getKeyFromJson(moviesJsonStr);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
//                TextView tvTrailer = (TextView)v.findViewById(R.id.tvTrailer);
//                tvTrailer.setText(result);
                trailerLink = result;
                if (trailerLink != null) {
                    btnTrailer.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detailfragment, menu);
        }
    }


}
