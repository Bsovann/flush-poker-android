package com.example.flush_poker_android.Client;
import java.util.List;

public interface GameUpdateListener {
    void onCommunityCardsUpdate(List<Integer> updatedCardImages);
    void onPlayerTurnUpdate();
    void onPotUpdate();
    // Add more methods for other updates as needed
}
