package app.m26.wikidriver.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import app.m26.wikidriver.R;

public class WebActivity extends AppCompatActivity {

    private String url, title;
    private Bundle extras;
    private WebView webView;
    private ImageView imgClose;
    private Toolbar toolbar;
    private TextView txtTitle, txtUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        toolbar = findViewById(R.id.toolbar);
        imgClose = findViewById(R.id.imgClose);
        txtTitle = findViewById(R.id.txtTitle);
        txtUrl = findViewById(R.id.txtUrl);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        extras = getIntent().getExtras();

        if(extras != null) {
            url = extras.getString("url");
            title = extras.getString("title");
        }

        txtTitle.setText(title);
        txtUrl.setText(url);
        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new MyBrowser());

        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.loadUrl(url);

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_browser_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_openChrome:
                openChrome();
                break;
            case R.id.action_copy:
                copyLink();
                break;
            case R.id.action_share:
                shareLink();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareLink() {

    }

    private void copyLink() {

    }

    private void openChrome() {

    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
