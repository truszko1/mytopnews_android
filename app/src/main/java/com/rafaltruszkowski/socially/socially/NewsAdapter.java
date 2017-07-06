package com.rafaltruszkowski.socially.socially;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsAdapterViewHolder> {

    private ArrayList<JSONObject> mNewsData;

    final private NewsAdapterOnClickHandler mClickHandler;

    final private String ARTICLE_AUTHOR_KEY = "author";
    final private String ARTICLE_TITLE_KEY = "title";
    final private String ARTICLE_URL_KEY = "url";
    final private String ARTICLE_IMAGE_URL_KEY = "urlToImage";
    final private String ARTICLE_PUBDATE_KEY = "publishedAt";

    interface NewsAdapterOnClickHandler {
        void onClick(String weatherForDay);
    }

    NewsAdapter(NewsAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @Override
    public NewsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.news_card;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);
        return new NewsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewsAdapterViewHolder holder, int position) {

        // each time a news card is created, get appropriate data and populate the card
        JSONObject currentArticle = mNewsData.get(position);
        String author = getArticleMetaForKey(currentArticle, ARTICLE_AUTHOR_KEY);
        String title = getArticleMetaForKey(currentArticle, ARTICLE_TITLE_KEY);
        String url = getArticleMetaForKey(currentArticle, ARTICLE_URL_KEY);
        String imageUrl = getArticleMetaForKey(currentArticle, ARTICLE_IMAGE_URL_KEY);

        holder.mNewsCardTitleTextView.setText(title);
        holder.mNewsCardAuthorTextView.setText(author);
        holder.mNewsCardSourceTextView.setText(Uri.parse(url).getHost());

        // also load the appropriate image, resize it to save resources in case original image is big
        Picasso.with((Context) mClickHandler)
                .load(imageUrl)
                .resize(250, 250)
                .centerCrop()
                .into(holder.mNewsCardImageView);


    }

    @Override
    public int getItemCount() {
        if (mNewsData == null) {
            return 0;
        } else {
            return mNewsData.size();
        }
    }

    class NewsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView mNewsCardTitleTextView;
        final TextView mNewsCardAuthorTextView;
        final TextView mNewsCardSourceTextView;
        final ImageView mNewsCardImageView;

        NewsAdapterViewHolder(View view) {
            super(view);
            mNewsCardTitleTextView = (TextView) view.findViewById(R.id.tv_news_card_title);
            mNewsCardAuthorTextView = (TextView) view.findViewById(R.id.tv_news_card_author);
            mNewsCardSourceTextView = (TextView) view.findViewById(R.id.tv_news_card_source);
            mNewsCardImageView = (ImageView) view.findViewById(R.id.iv_news_card_image);

            // set up an on click listener for the cards, implemented by MainActivity
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();

            // send the url back to the main activity
            mClickHandler.onClick(getArticleMetaForKey(mNewsData.get(adapterPosition), ARTICLE_URL_KEY));
        }
    }

    void setNewsData(ArrayList<JSONObject> newsData) {

        // purify the data
        // if a story coming through has no image or a title, do not show it to the user
        ArrayList<JSONObject> cleanNewsData = new ArrayList<>();
        if (newsData != null) {

            for (JSONObject article :
                    newsData) {
                String title = getArticleMetaForKey(article, ARTICLE_TITLE_KEY);
                String imageUrl = getArticleMetaForKey(article, ARTICLE_IMAGE_URL_KEY);
                if (title.length() != 0 && imageUrl.length() != 0) {
                    cleanNewsData.add(article);
                }
            }
        }

        // update the view
        mNewsData = cleanNewsData;
        notifyDataSetChanged();
    }

    private String getArticleMetaForKey(JSONObject currentArticle, String article_author_key) {
        String value = "";
        try {
            value = currentArticle.getString(article_author_key);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return value.trim();
    }
}
