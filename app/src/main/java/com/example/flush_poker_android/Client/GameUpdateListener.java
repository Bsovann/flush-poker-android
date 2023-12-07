/*
 * Author: Bondith Sovann
 * Description: This interface defines the methods for handling game updates in a poker game.
 */
package com.example.flush_poker_android.Client;

import java.util.List;

public interface GameUpdateListener {
    void onCommunityCardsUpdate(List<Integer> updatedCardImages);
    void onPlayerTurnUpdate();
    void onPotUpdate();
    // Add more methods for other updates as needed
}
