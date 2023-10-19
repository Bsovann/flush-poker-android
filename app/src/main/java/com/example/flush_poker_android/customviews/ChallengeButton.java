package com.example.flush_poker_android.customviews;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.flush_poker_android.R;

public class ChallengeButton extends LinearLayout {
    public ChallengeButton(Context context) {
        super(context);
        init();
    }

    public ChallengeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.challenge_button, this, true);
    }
}

