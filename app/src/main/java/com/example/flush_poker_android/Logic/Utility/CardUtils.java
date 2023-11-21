package com.example.flush_poker_android.Logic.Utility;

import android.content.Context;

public class CardUtils {
    public static int getCardImageResourceId(String cardName, Context context) {
        String resourceName = cardName;
        // Get the resource ID dynamically using the generated R.drawable class
        int resourceId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());

        return resourceId;
    }
}
