package stories.spectrum.huji.ac.il.stories.views;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class StoriesTextView extends AppCompatTextView {

    public StoriesTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public StoriesTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (!isInEditMode()) {

            // Set Font
            CustomFontHelper.setCustomFont(this, context, attrs);
        }
    }

}