package com.rafaltruszkowski.socially.socially.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.rafaltruszkowski.socially.socially.utils.Network;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DefaultSettings {

    public static final String[] NEWS_SOURCES = new String[]{
            "business-insider",
            "cnn",
            "the-huffington-post",
            "the-wall-street-journal",
            "usa-today",
            "the-washington-post",
            "bloomberg"
    };

    private static final String API_SCHEME = "http";
    private static final String API_HOST = "newsapi.org";

    private static final String API_VERSION = "v1";

    private static final String API_REQUEST_KEY_SOURCE = "source";
    private static final String API_REQUEST_KEY_SORT_BY = "sortBy";
    private static final String API_REQUEST_KEY_API = "apiKey";

    private static final String API_ACCESS_KEY = "bc8ed1421a6a48a39b1695ee0697bcb0";

    public static URL getArticlesAPI(String source, Context context) {
        String[] paths = new String[]{API_VERSION, "articles"};

        Map<String, String> queries = new HashMap<>();
        queries.put(API_REQUEST_KEY_SOURCE, source);
        queries.put(API_REQUEST_KEY_SORT_BY, getPreferredOrderBy(context));
        queries.put(API_REQUEST_KEY_API, API_ACCESS_KEY);

        String fragment = "";

        return Network.buildUrl(API_SCHEME, API_HOST, paths, queries, fragment);
    }

    public static ArrayList<String> getPreferredNewsSources(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        ArrayList<String> sources = new ArrayList<>();

        // get saved preferences for the selection of news sources
        for (String newsSource : NEWS_SOURCES) {
            if (prefs.getBoolean(newsSource, false)) {
                sources.add(newsSource);
            }
        }

        return sources;
    }

    private static String getPreferredOrderBy(Context context) {
        return "top";
    }

}
