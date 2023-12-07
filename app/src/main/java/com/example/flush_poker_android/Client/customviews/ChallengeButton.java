/**
 * Author: Bondith Sovann
 * Description: Custom view class representing a ChallengeButton, which is a specialized LinearLayout
 * used for a specific UI component.
 */
package com.example.flush_poker_android.Client.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

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

    /**
     * Author: Bondith Sovann
     * Description: Initializes the ChallengeButton view by inflating the corresponding layout resource.
     */
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.challenge_button, this, true);
    }
}
