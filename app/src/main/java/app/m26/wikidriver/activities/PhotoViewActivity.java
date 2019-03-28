package app.m26.wikidriver.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import app.m26.wikidriver.R;;
import com.squareup.picasso.Picasso;

public class PhotoViewActivity extends AppCompatActivity {

    private ImageView imgPhoto;
    private Intent extras;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        extras = getIntent();
        imgPhoto = findViewById(R.id.imgFullPhoto);

        if (extras != null) {
            String url = extras.getExtras().getString("image");
            Picasso.with(getApplicationContext()).load(url).into(imgPhoto);
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
