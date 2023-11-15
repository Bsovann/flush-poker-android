package com.example.flush_poker_android.Client.Utility;

import android.content.Context;

public class CardUtils {
    public static int getCardImageResourceId(String cardName, Context context) {
        // Assuming your card names are in the format 'ace_of_clubs'
        // Convert the card name to lowercase and replace underscores with lowercase letters
        String resourceName = cardName.toLowerCase().replace("_", "");

        // Get the resource ID dynamically using the generated R.drawable class
        return context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
    }
}
