package app.m26.wikidriver.custom;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

public class CustomFloatingActionButton extends FloatingActionButton {

    public CustomFloatingActionButton(Context context) {
        super(context);
    }

    public CustomFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //TODO add pulse view to fab.
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        setMeasuredDimension((int) (width * 1.2f), (int) (height * 1.2f));
    }
}
