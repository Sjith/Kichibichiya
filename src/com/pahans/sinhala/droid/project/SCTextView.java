package com.pahans.sinhala.droid.project;


import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


public class SCTextView extends TextView {
    private static Typeface tf;

	public SCTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SCTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SCTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
        	if(tf==null)
        		tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/MalithiWeb.ttf");
            setTypeface(tf);
        }
    }
}



