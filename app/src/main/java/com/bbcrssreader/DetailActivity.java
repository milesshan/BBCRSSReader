package com.bbcrssreader;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * DetailActivity
 *
 * Activity used to display detailed information for each list view item
 */
public class DetailActivity extends AppCompatActivity {

    TextView textView_title, textView_description, textView_link, textView_guid, textView_date;

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        textView_title = findViewById(R.id.activity_detail_textView_title);
        textView_description = findViewById(R.id.activity_detail_textView_description);
        textView_link = findViewById(R.id.activity_detail_textView_link);
        textView_guid = findViewById(R.id.activity_detail_textView_guid);
        textView_date = findViewById(R.id.activity_detail_textView_date);

        Intent intent = this.getIntent();

        String title = intent.getExtras().getString("TITLE_KEY");
        String desc = intent.getExtras().getString("DESCRIPTION_KEY");
        String date = intent.getExtras().getString("DATE_KEY");
        String guid = intent.getExtras().getString("GUID_KEY");
        String link = intent.getExtras().getString("LINK_KEY");

        textView_title.setText(title);
        textView_description.setText(Html.fromHtml(desc));
        textView_link.setText(link);
        textView_guid.setText(guid);
        textView_date.setText(date);
    }
}
