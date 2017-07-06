package com.rafaltruszkowski.socially.socially;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.rafaltruszkowski.socially.socially.data.DefaultSettings;
import com.rafaltruszkowski.socially.socially.utils.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainActivity extends AppCompatActivity implements
        NewsAdapter.NewsAdapterOnClickHandler, LoaderManager.LoaderCallbacks<ArrayList<JSONObject>>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int NEWS_LOADER_ID = 0;

    private NewsAdapter mNewsAdapter;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        // I think a bug does not real default values on initial load
        // https://issuetracker.google.com/issues/36914268
        // this forces the system to load initial values
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // initialize the loaders each time the activity is created
        LoaderManager.LoaderCallbacks<ArrayList<JSONObject>> callback = MainActivity.this;
        getSupportLoaderManager().initLoader(NEWS_LOADER_ID, null, callback);

        // set up the recycler view with vertical layout
        int recyclerViewOrientation = LinearLayoutManager.VERTICAL;
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, recyclerViewOrientation, false);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_news_cards);
        mRecyclerView.setLayoutManager(layoutManager);
        // size of cards is fixed, this improved performance
        mRecyclerView.setHasFixedSize(true);
        mNewsAdapter = new NewsAdapter(this);
        mRecyclerView.setAdapter(mNewsAdapter);
    }

    // load news stories in async, on a thread separate from UI
    @Override
    public Loader<ArrayList<JSONObject>> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<ArrayList<JSONObject>>(this) {

            @Override
            protected void onStartLoading() {
                mProgressBar.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Override
            public ArrayList<JSONObject> loadInBackground() {
                ArrayList<String> newsSources = DefaultSettings.getPreferredNewsSources(MainActivity.this);
                ArrayList<JSONObject> articles = new ArrayList<>();

                // since we load news sources one by one, use offset to insert stories one by one,
                // otherwise users would see all stories from one source until gettings stories from another source
                int offset = 0;

                for (String source : newsSources) {
                    URL articlesAPI = DefaultSettings.getArticlesAPI(source, MainActivity.this);

                    try {
                        String result = Network.getResponseFromHttpUrl(articlesAPI);

                        JSONObject json = new JSONObject(result);

                        JSONArray articlesFromJSON = json.getJSONArray("articles");

                        for (int i = 0; i < articlesFromJSON.length(); i++) {
                            JSONObject article = articlesFromJSON.getJSONObject(i);
                            articles.add(i + i * offset, article);
                        }

                        offset++;

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }

                return articles;
            }
        };
    }

    // when async loading is finished, send the data to the adapter
    @Override
    public void onLoadFinished(Loader<ArrayList<JSONObject>> loader, ArrayList<JSONObject> data) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mNewsAdapter.setNewsData(data);

    }

    // unused at this time
    @Override
    public void onLoaderReset(Loader<ArrayList<JSONObject>> loader) {
    }

    // when a user clicks a story card, open the browser with that story
    @Override
    public void onClick(String articleURL) {
        Uri webpage = Uri.parse(articleURL);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            mNewsAdapter.setNewsData(null);
            getSupportLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);
            return true;
        }
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // any time a Settings option is changed, reload stories; user has changed selection of news sources
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mNewsAdapter.setNewsData(null);
        getSupportLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);
    }

}
