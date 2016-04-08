package com.voyager.popmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MoviesFragment extends Fragment {

    SharedPreferences prefs;
    View rootView;
    GridViewAdapter gvAdapter;
    GridView gv;

    public MoviesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        gv = (GridView) rootView.findViewById(R.id.grid_view);

        return rootView;
    }

    private void updateMovies() {
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String movies_number = prefs.getString(getString(R.string.pref_movies_number_key),
                getString(R.string.pref_movies_number_default));
        moviesTask.execute(Integer.parseInt(movies_number));
    }

    @Override
    public void onStart() {
        super.onStart();
//        tableLayout = (TableLayout) getActivity().findViewById(R.id.tableLayout);
        updateMovies();
    }

    public class FetchMoviesTask extends AsyncTask<Integer, Void, JSONObject[]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        String sortingMethod;

        private JSONObject[] getMoviesDataFromJson(String moviesJsonStr, Integer numOfMovies)
                throws JSONException {

            final String RESULTS = "results";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(RESULTS);

            if (numOfMovies > moviesArray.length())
                numOfMovies = moviesArray.length();
            JSONObject[] resultJsonObj = new JSONObject[numOfMovies];

            for(int i = 0; i < numOfMovies; i++) {
//                String title;
//                String rating="";
//                String rating_key;
//
//                final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185/";
//                final String TOP_RATED = "top_rated";
//                final String MOST_POPULAR = "popular";

                JSONObject movie = moviesArray.getJSONObject(i);
//                title = movie.getString("title");
//                posters[i] = POSTER_BASE_URL + movie.getString(poster_key).substring(1);

//                switch(sortingMethod) {
//                    case TOP_RATED:
//                        rating_key = "vote_average";
//                        rating = "[vote average]: " + movie.getDouble(rating_key);
//                        break;
//                    case MOST_POPULAR:
//                        rating_key = "popularity";
//                        rating = "[popularity]: " + movie.getDouble(rating_key);
//                        break;
//                    default: rating_key = null;
//                }

//                resultStrs[i] = title + "\n" + rating;
                resultJsonObj[i] = movie;
            }

            return resultJsonObj;
        }

        @Override
        protected JSONObject[] doInBackground(Integer... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String moviesJsonStr = null;

            try {
                final String MOVIES_BASE_URL =
                        "http://api.themoviedb.org/3/movie/";
                final String KEY_PARAM = "api_key";

                sortingMethod = prefs.getString(
                        getString(R.string.pref_sorting_key),
                        getString(R.string.pref_sorting_top_rated));

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendPath(sortingMethod)
                        .appendQueryParameter(KEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

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
                return getMoviesDataFromJson(moviesJsonStr, params[0]);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(final JSONObject[] result) {
            String[] posters = new String[result.length];;
            final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185/";
            String poster_key = "poster_path";
            for(int i = 0; i < result.length; i++) {
                JSONObject movie = result[i];
                try {
                    posters[i] = POSTER_BASE_URL + movie.getString(poster_key).substring(1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            gvAdapter = new GridViewAdapter(getActivity(), posters);
            gv.setAdapter(gvAdapter);
            gv.setOnScrollListener(new PicassoScrollListener(getActivity()));

            gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    JSONObject forecast = result[position];
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .putExtra(Intent.EXTRA_TEXT, forecast.toString());
                    startActivity(intent);
                }
            });

        }
    }
 }
