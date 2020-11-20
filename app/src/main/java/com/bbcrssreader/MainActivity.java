package com.bbcrssreader;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Main activity to be loaded on launch
 */
public class MainActivity extends AppCompatActivity {

    final static String bbcRssUrl = "https://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml";
    ProgressBar progressBar;
    ListView listView;

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.activity_main_button_progressBar);
        progressBar.setVisibility(View.VISIBLE);

        listView = findViewById(R.id.activity_main_listview);

        //TODO - Implement favourite
        Button buttonSave = findViewById(R.id.activity_main_button_save);
        buttonSave.setOnClickListener((click) -> Toast.makeText(this, "Favourited article!", Toast.LENGTH_LONG).show());

        EditText editText = findViewById(R.id.activity_main_editText_edit);

        //TODO - Implement search
        Button buttonSearch = findViewById(R.id.activity_main_button_search);
        buttonSearch.setOnClickListener((click) -> {
            Snackbar snackbar = Snackbar.make(editText, "Searching for keyword", Snackbar.LENGTH_LONG);
            snackbar.show();
        });

        new RSSDownloader(MainActivity.this).execute();
    }

    /**
     * Class to download and parse xml from website
     */
    private class RSSDownloader extends AsyncTask<String, Integer, String> {
        private final Context context;
        private URL urlAddress;
        private final ArrayList<Article> articles = new ArrayList<>();

        public RSSDownloader(Context context) {
            this.context = context;
        }

        /**
         * doInBackground
         */
        @Override
        protected String doInBackground(String... args) {
            try {
                // Open new connection and set connection parameters
                urlAddress = new URL(bbcRssUrl);
                HttpURLConnection rssUrlConnection = (HttpURLConnection) urlAddress.openConnection();
                rssUrlConnection.setRequestMethod("GET");
                rssUrlConnection.setConnectTimeout(10000);
                rssUrlConnection.setReadTimeout(10000);
                rssUrlConnection.setDoInput(true);

                // Instantiate XmlPullParser to prepare to parse RSS feed xml
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xmlPullParser = factory.newPullParser();
                InputStream rssResponse = rssUrlConnection.getInputStream();
                xmlPullParser.setInput(rssResponse, "UTF-8");

                int eventType = xmlPullParser.getEventType();

                String tagValue = null;
                boolean itemEnded = true;

                articles.clear();
                Article article = new Article();

                // Loop through the xml file until end of document
                do {
                    String tagName = xmlPullParser.getName();

                    // Search for "item" start tag
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (tagName.equalsIgnoreCase("item")) {
                                article = new Article();
                                itemEnded = false;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            tagValue = xmlPullParser.getText();
                            break;

                        // Get all descriptors
                        case XmlPullParser.END_TAG:
                            if (!itemEnded) {
                                if (tagName.equalsIgnoreCase("title")) {
                                    article.setTitle(tagValue);
                                    publishProgress(20);
                                } else if (tagName.equalsIgnoreCase("description")) {
                                    article.setDescription(tagValue);
                                    publishProgress(40);
                                } else if (tagName.equalsIgnoreCase("link")) {
                                    article.setLink(tagValue);
                                    publishProgress(60);
                                } else if (tagName.equalsIgnoreCase("guid")) {
                                    article.setGuid(tagValue);
                                    publishProgress(80);
                                } else if (tagName.equalsIgnoreCase("pubDate")) {
                                    article.setDate(tagValue);
                                    publishProgress(100);
                                }
                            }

                            // Before hitting the end "item" tag
                            if (tagName.equalsIgnoreCase("item")) {
                                articles.add(article);
                                itemEnded = true;
                            }

                            break;
                    }

                    eventType = xmlPullParser.next();

                } while (eventType != XmlPullParser.END_DOCUMENT);
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }

            return "Done";
        }

        /**
         * onProgressUpdate
         *
         * Update progress bar
         */
        @Override
        protected void onProgressUpdate(Integer... args) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(args[0]);
        }

        /**
         * onPostExecute
         *
         * Inflate list view using list view adapter
         */
        @Override
        protected void onPostExecute(String fromDoInBackground) {
            progressBar.setVisibility(View.INVISIBLE);
            listView.setAdapter(new RssAdapter(context, R.layout.activity_main, articles));
        }

        /**
         * RssAdapter
         *
         * Adapter for list view
         */
        private class RssAdapter extends ArrayAdapter<Article> {
            public RssAdapter(Context context, int layoutResource, List<Article> articles) {
                super(context, layoutResource, articles);
            }

            /**
             * getCount
             */
            @Override
            public int getCount() {
                return articles.size();
            }

            /**
             * getItem
             */
            @Override
            public Article getItem(int position) {
                return articles.get(position);
            }

            /**
             * getItemId
             */
            @Override
            public long getItemId(int position) {
                return position;
            }

            /**
             * getView
             *
             * Inflate list_item layout and set attribute values
             */
            @Override
            public View getView(int position, View view, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.list_item, parent, false);

                TextView textView_title = view.findViewById(R.id.list_item_textView_title);

                Article listArticle = this.getItem(position);

                final String title = listArticle.getTitle();
                final String description = listArticle.getDescription();
                final String date = listArticle.getDate();
                final String guid = listArticle.getGuid();
                final String link = listArticle.getLink();

                textView_title.setText(title);

                // On click of list view item, move to next activity
                view.setOnClickListener((v) -> {
                    Intent intent = new Intent(context, DetailActivity.class);

                    intent.putExtra("TITLE_KEY", title);
                    intent.putExtra("DESCRIPTION_KEY", description);
                    intent.putExtra("LINK_KEY", link);
                    intent.putExtra("GUID_KEY", guid);
                    intent.putExtra("DATE_KEY", date);

                    context.startActivity(intent);
                });

                return view;
            }
        }
    }
}
