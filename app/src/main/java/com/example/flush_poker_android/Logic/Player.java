package com.example.flush_poker_android.Logic;

public interface Player {
    public void setController(GameController controller);
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
}
