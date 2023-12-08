package com.example.flush_poker_android.Logic;

import java.util.List;

/**Interface with methods implemented in Bot, Client, & Controller Player
 *
 * @author Bondith Sovann*/
public interface Player {
    public void setController(PracticeModeGameController controller);
    public String getPlayerAction();
    public Boolean hasFolded();
    public void setAvailableActions(int currentBet);
    public void check();
    public void bet(int amount);
    public void fold();
    public void raise(int amount);
    public void setCurrentBet(int bet);
    public void addToChipCount(int pot);
    public int getChips();
    public int getCurrentBet();
    public String getName();
    public boolean actionIsDone();
    public void setPlayerAction(String action);
    public void decreaseChips(int amount);
    public void setActionIsDone(boolean b);
    public void addCard(Card dealCard);
    public List<? extends Card> getHand();
    int compareHands(List<Card> communityCards);
    void playerStateReset();
    int getBetAmount();
    void setActionListener(PlayerActionListener listener);
    /**Get Current list of actions available to the player*/
    List<String> getAvailableActions();
    void call();
    void setHasFold(boolean b);
}
