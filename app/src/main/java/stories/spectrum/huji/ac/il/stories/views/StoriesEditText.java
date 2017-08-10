package stories.spectrum.huji.ac.il.stories.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import stories.spectrum.huji.ac.il.stories.R;

public class StoriesEditText extends AppCompatEditText {

    public StoriesEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public StoriesEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (!isInEditMode()) {

            // Set Font
            CustomFontHelper.setCustomFont(this, context, attrs);

//            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Lato-Regular.ttf");
//            setTypeface(tf);

            // Set Color
            Drawable nd = getBackground().getConstantState().newDrawable();
            nd.setColorFilter(AppCompatDrawableManager.getPorterDuffColorFilter(
                    ContextCompat.getColor(getContext(), R.color.colorOrange), PorterDuff.Mode.SRC_IN));
            setBackground(nd);
        }
    }
}