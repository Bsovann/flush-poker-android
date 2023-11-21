package com.example.flush_poker_android.Client.Utility;

import android.content.Context;
import android.util.Log;

public class CardUtils {
    public static int getCardImageResourceId(String cardName, Context context) {
        // Add debug logging
        Log.d("CardUtils", "getCardImageResourceId: cardName = " + cardName + ", context = " + context);

        // Assuming your card names are in the format 'ace_of_clubs'
        // Convert the card name to lowercase and replace underscores with lowercase letters
        String resourceName = cardName;

        // Get the resource ID dynamically using the generated R.drawable class
        int resourceId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());

        // Add debug logging
        Log.d("CardUtils", "getCardImageResourceId: resourceId = " + resourceId);

        return resourceId;
    }
}
