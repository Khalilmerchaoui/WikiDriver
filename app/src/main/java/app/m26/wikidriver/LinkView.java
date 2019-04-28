package app.m26.wikidriver;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.m26.wikidriver.R;;

import app.m26.wikidriver.models.Link;
import com.squareup.picasso.Picasso;

public class LinkView extends LinearLayout {

    private TextView txtTitle, txtUrl;
    private ImageView img;
    private Link link;

    public LinkView(Context context) {
        super(context);
    }

    public LinkView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LinkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public LinkView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(getContext(), R.layout.link_item_layout, this);
        txtTitle = findViewById(R.id.txtTitle);
        txtUrl = findViewById(R.id.txtUrl);
        img = findViewById(R.id.img);

    }

    public void setLinkInfo(Context context, Link link) {
        this.link = link;

        txtTitle.setText(link.getTitle());
        txtUrl.setText(link.getUrl());

        Picasso.with(img.getContext())
                .load(link.getImgUrl())
                .fit()
                .centerCrop()
                .error(R.drawable.image_not_loaded)
                .into(img);
    }

    public Link getLinkInfo() {
        return link;
    }

    public void setLinkLocalImage(Context context, Link link) {
        this.link = link;

        txtTitle.setText(link.getTitle());
        txtUrl.setText(link.getUrl());
        img.setBackgroundResource(R.drawable.facebook_image);

    }
}
