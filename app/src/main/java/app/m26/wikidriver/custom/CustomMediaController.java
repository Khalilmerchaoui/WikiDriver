package app.m26.wikidriver.custom;

import android.content.Context;
import android.view.View;
import android.widget.MediaController;

public class CustomMediaController extends MediaController {

    public CustomMediaController(Context context) {
        super(context);
    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);
    }
}
