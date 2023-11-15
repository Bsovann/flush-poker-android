package com.example.flush_poker_android.Client;
import java.util.List;

public interface GameUpdateListener {
    void onCardUpdate(List<Integer> updatedCardImages);
    // Add more methods for other updates as needed
}
