package com.mobcomlab.firebots.Views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class TextViewTitle extends android.support.v7.widget.AppCompatTextView {

    public TextViewTitle(Context context) {
        super(context);
        init();
    }

    public TextViewTitle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextViewTitle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/Bebas Neue.ttf");
        this.setTypeface(font);
    }

    @Override
    public void setTypeface(Typeface tf, int style) {
        tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Bebas Neue.ttf");
        super.setTypeface(tf, style);
    }

    @Override
    public void setTypeface(Typeface tf) {
        tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Bebas Neue.ttf");
        super.setTypeface(tf);
    }
}