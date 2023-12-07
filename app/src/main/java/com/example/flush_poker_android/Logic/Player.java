package com.example.flush_poker_android.Logic;

import java.util.List;

public interface Player {
    void setController(PracticeModeGameController controller);
    String getPlayerAction();
    Boolean hasFolded();
    void setAvailableActions(int currentBet);
    void check();
    void bet(int amount);
    void fold();
    void raise(int amount);
    void setCurrentBet(int bet);
    void addToChipCount(int pot);
    int getChips();
    int getCurrentBet();
    String getName();
    boolean actionIsDone();
    void setPlayerAction(String action);
    void decreaseChips(int amount);
    void setActionIsDone(boolean b);
    void addCard(Card dealCard);
    List<? extends Card> getHand();
    int compareHands(List<Card> communityCards);
    void playerStateReset();
    int getBetAmount();
    void setActionListener(PlayerActionListener listener);
    List<String> getAvailableActions();
    void call();
    void setHasFold(boolean b);
}
