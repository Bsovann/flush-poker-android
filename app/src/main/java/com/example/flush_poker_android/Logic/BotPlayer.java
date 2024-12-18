package com.example.flush_poker_android.Logic;
import android.content.Context;
import android.os.Handler;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class represents a Bot Player in a poker game.
 * It implements the Player interface and provides AI logic for making decisions.
 *
 * @author Bondith Sovann
 */
public class BotPlayer extends Hand implements Player, Runnable, Serializable{
    private final String name;
    private int currentBet;
    private String playerAction;
    private List<String> availableActions;
    private Boolean hasFold = false;
    private Boolean actionIsDone = false;
    private int chips;
    private PracticeModeGameController controller;
    private final Handler handlerUi;
    private final Context context;
    private int betAmount = 0;

    /**
     * Constructor for creating a BotPlayer instance.
     *
     * @param name    The name of the bot player.
     * @param chips   The initial number of chips the bot player has.
     * @param handler A handler for handling UI updates.
     * @param context The Android application context.
     */
    public BotPlayer(String name, int chips, Handler handler, Context context) {
        super(); // Hand, parent's class.
        this.name = name;
        this.chips = chips;
        this.playerAction = "";
        this.availableActions = new ArrayList<>();
        this.handlerUi = handler;
        this.context = context;
    }

    /**
     * The main logic for the bot player's actions during the game.
     * It implements the Runnable interface to run in a separate thread.
     */
    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                try {
                    this.wait(); // Wait for the controller to signal your turn
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Perform actions for your turn
                makeAutoDecision();

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                synchronized (controller){
                    // Signal the controller that your turn is done
                    actionIsDone = true;
                    controller.notify();
                }
            }
        }
    }
    /**
     * Method for making automatic decisions for the bot player.
     * The AI logic for decision-making is based on random choices.
     */
    public void makeAutoDecision() {
        // This is a basic AI logic for automatic decision-making.
        // You can make it more sophisticated based on your game rules.

        int minRaise = this.currentBet + 1;
        int maxRaise = this.chips;

        Random random = new Random();
        int decision = random.nextInt(availableActions.size()); // Generate a random decision (0 to 3)

        String playerAction = availableActions.get(decision);

        if (playerAction.equals("Fold")) {
            // 25% chance to fold
            fold();
            setPlayerAction("Fold");
        } else if (playerAction.equals("Check")) {
            // 25% chance to check
            check();
            setPlayerAction("Check");
        } else if (playerAction.equals("Call")) {
            // 25% chance to call
            call();
            setPlayerAction("Call");
        } else {
            // 25% chance to raise
            int raiseAmount = random.nextInt(maxRaise - minRaise + 1) + currentBet;
            bet(raiseAmount);
            setPlayerAction("Raise");
        }

        // Signal that the AI has completed its action
        actionIsDone = true;
    }
    /**
     * Compares the player's hand with the community cards.
     *
     * @param communityCards The community cards to compare with.
     * @return An integer indicating the result of the hand comparison.
     */
    @Override
    public int compareHands(List<Card> communityCards) {
        return super.compareHands(communityCards);
    }
    @Override
    public void setController(PracticeModeGameController controller){
        this.controller = controller;
    }
    @Override
    public String getPlayerAction(){
        return this.playerAction;
    }
    @Override
    public Boolean hasFolded(){
        return hasFold;
    }
    @Override
    public void setAvailableActions(int currentBet) {
        List<String> actions = new ArrayList<>();
        this.currentBet = currentBet;

        // Check if the player can Fold
        if (!hasFold) {
            actions.add("Fold");
        }
        // Check if the player can Check (bet 0 if no one has raised)
        if (this.currentBet == 0) {
            actions.add("Check");
        }

        // Check if the player can Call (match the current bet)
        if (this.currentBet > 0 && this.currentBet <= this.chips) {
            actions.add("Call");
        }

        // Check if the player can Raise (if they have enough chips)
        int minimumRaise = this.currentBet + 1; // Minimum raise amount
        int maximumRaise = this.chips; // Maximum raise is their chip stack
        if (this.currentBet > 0 && minimumRaise <= maximumRaise) {
            actions.add("Raise");
        }
        this.availableActions = actions;
    }
    @Override
    public int getBetAmount() {
        return betAmount;
    }
    @Override
    public void setActionListener(PlayerActionListener listener) {
    }
    @Override
    public List<String> getAvailableActions() {
        return null;
    }
    @Override
    public void call() {
        bet(currentBet);
    }

    @Override
    public void setHasFold(boolean b) {
        this.hasFold = b;
    }

    public void playerStateReset(){
        this.playerAction = "";
        this.actionIsDone = false;
        this.availableActions.clear();
        this.hasFold = false;
        this.clearHand();
    }
    @Override
    public void check() {
        // Implement checking logic
        // In poker, checking means that the player does not want to bet but wants to stay in the game.
        // You need to implement the logic to determine if a check is allowed based on the game state.
        this.betAmount = 0;
    }
    @Override
    public void bet(int amount) {
        // Implement betting logic
        // This method should handle the player placing a bet with the specified amount.
        // Ensure that the player has enough credits to make the bet, and deduct the amount from their credits.
        if(chips >= amount) {
            this.chips -= amount;
            this.betAmount = amount;
        }
    }
    @Override
    public void fold() {
        // Implement folding logic
        // Folding means the player chooses to forfeit the current hand and not participate further.
        // You should update the game state to reflect that this player has folded.
        this.hasFold = true;
    }
    @Override
    public void raise(int amount){
        int total = currentBet + amount;
        bet(total);
    }
    @Override
    public void setCurrentBet(int bet){
        this.currentBet = bet;
    }
    @Override
    public void addToChipCount(int pot) {
        this.chips += pot;
    }
    @Override
    public int getChips() {
        return this.chips;
    }
    @Override
    public int getCurrentBet() {
        return this.currentBet;
    }
    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public boolean actionIsDone() {
        return this.actionIsDone;
    }
    @Override
    public void setPlayerAction(String action) {
        this.playerAction = action;
        this.actionIsDone = true;
    }
    @Override
    public void decreaseChips(int amount) {
        this.chips -= amount;
    }

    @Override
    public void setActionIsDone(boolean b) {
        this.actionIsDone = b;
    }



}

