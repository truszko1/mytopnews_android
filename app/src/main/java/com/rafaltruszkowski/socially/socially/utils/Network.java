package com.rafaltruszkowski.socially.socially.utils;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class Network {

    public static URL buildUrl(String scheme, String authority, String[] paths, Map<String, String> queries, String fragment) {
        Uri.Builder builder = new Uri.Builder();

        // add scheme
        builder.scheme(scheme)
                .authority(authority);

        // add paths
        for (String path :
                paths) {
            builder.appendPath(path);
        }

        // add queries
        if (queries != null) {
            Iterator it = queries.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                builder.appendQueryParameter((String) pair.getKey(), (String) pair.getValue());
                it.remove(); // avoids a ConcurrentModificationException
            }
        }

        // add fragment
        builder.fragment(fragment);

        Uri uri = builder.build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }


    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);

            // this forces the scanner to read the entire contents of the stream into the next token stream
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
