package com.example.flush_poker_android;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.flush_poker_android.Logic.Utility.CardUtils;

import org.junit.Test;

/**@author Bondith Sovann*/
public class CardUtilsTest {

    @Test
    public void testGetCardImageResourceId() {
        // Context of the app under test.
        Context context = ApplicationProvider.getApplicationContext();

        // Test case 1: Valid card name
        int resourceId1 = CardUtils.getCardImageResourceId("ace_of_clubs", context);
        assertEquals(R.drawable.ace_of_clubs, resourceId1);

        // Test case 2: Valid card name with different suit
        int resourceId2 = CardUtils.getCardImageResourceId("king_of_diamonds", context);
        assertEquals(R.drawable.king_of_diamonds, resourceId2);

        // Test case 3: Card name with spaces and mixed case
        int resourceId3 = CardUtils.getCardImageResourceId("Queen_of_Spades", context);
        assertEquals(R.drawable.queen_of_spades, resourceId3);

        // Test case 4: Invalid card name
        int resourceId4 = CardUtils.getCardImageResourceId("invalid_card_name", context);
        assertEquals(0, resourceId4); // Assuming 0 is the default resource ID for invalid cases
    }
}

