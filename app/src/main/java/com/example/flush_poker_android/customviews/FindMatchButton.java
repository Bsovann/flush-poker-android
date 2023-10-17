package com.example.flush_poker_android.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.flush_poker_android.R;

public class FindMatchButton extends RelativeLayout {
    public FindMatchButton(Context context) {
        super(context);
        init();
    }

    public FindMatchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // Action here
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.find_match_button, this, true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Find Match Button", "Bondith CLick!");
            }
        });
    }
}


