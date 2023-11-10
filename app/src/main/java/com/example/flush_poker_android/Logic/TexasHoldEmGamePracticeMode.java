package com.example.flush_poker_android.Logic;

import static android.os.Looper.getMainLooper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.flush_poker_android.Client.GameActivity;

import java.util.ArrayList;
import java.util.List;

public class TexasHoldEmGamePracticeMode extends BroadcastReceiver {
    private Deck deck;
    private List<Player> players;
    private List<Card> communityCards;
    private List<Player> bettingRound;

    private Player currentPlayer;
    private int dealerPosition;

    // 4. Set up the initial game state.
    private int smallBlind;
    private int bigBlind;
    private int currentBet;
    private int pot;
    private Player dealer;
    private int currentPlayerIndex;
    private BettingLogic bettingLogic;

    private GameActivity gameActivity;
    private Player winner;

    private int turnTimeInSeconds = 30;
    private final Handler handlerUI;
    private final android.os.Handler handler = new Handler(Looper.getMainLooper());

    private Context context;

    public TexasHoldEmGamePracticeMode(Context context, Handler handler) {

        // Register the BroadcastReceiver with the desired action(s)
        IntentFilter intentFilter = new IntentFilter("com.example.flush_poker_android.ACTION");
        context.registerReceiver(this, intentFilter);
        this.context = context;
        this.handlerUI = handler;

        // Initialize game logic
        initializeGame();
    }

    public TexasHoldEmGamePracticeMode() {
        this.handlerUI = handler;
        // Initialize game logic
        initializeGame();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getStringExtra("action");

            if ("Bet".equals(action)) {
                // Handle button click action
                // Update the game state or perform other logic
                Log.i("Action", "Bet from Logic class");
            } else if ("Raise".equals(action)) {
                // Handle button click action
                // Update the game state or perform other logic
            } else if ("Check".equals(action)) {
                // Handle button click action
                // Update the game state or perform other logic
            } else if ("Call".equals(action)) {
                // Handle button click action
                // Update the game state or perform other logic
            } else if ("Fold".equals(action)) {
                // Handle button click action
                // Update the game state or perform other logic
            } else {
                // Exit Button
            }
        }
    }

    private void initializeGame() {
        this.deck = new Deck();
        this.players = new ArrayList<>(5);
        this.communityCards = new ArrayList<>();
        this.dealerPosition = 0;

        // We can Add addPlayer function for the real multiplayer one But in this case it's just practice mode
        players.add(new Player("Player0", 2000));
        players.add(new Player("Player1", 2000));
        players.add(new Player("Player2", 2000));
        players.add(new Player("Player3", 2000));
        players.add(new Player("Player4", 2000));

        // DeckShuffle
        deck.shuffle();

        // Initial Game State
        smallBlind = 10;
        bigBlind = 20;
        currentBet = bigBlind;
        pot = 0;
        dealer = players.get(dealerPosition);
        currentPlayerIndex = (players.indexOf(dealer) + 1) % players.size();
        currentPlayer = players.get(currentPlayerIndex);

        // Betting Logic
        bettingLogic = new BettingLogic(this.currentBet, this.pot, this.players);

    }

    public void startGame() {

        // 5. Start the game loop.
        while (players.size() > 1) {
            // Initialize hands
            dealTwoHoleCardsToPlayers();

            // Deal community cards and handle betting rounds.
            if (communityCards.size() == 0) {
                // Flop
                communityCards.addAll(deck.draw(3));
            } else if (communityCards.size() == 3) {
                // Turn
                communityCards.add(deck.dealCard());
            } else if (communityCards.size() == 4) {
                // River
                communityCards.add(deck.dealCard());
            }

            // Implement betting logic for each player's turn.
            // Players can choose to fold, call, or raise.
            // Update the current bet, pot, and player actions accordingly.
            startPlayerTurn();


            // Check for showdown and determine the winner.
            if (communityCards.size() == 5) {
                this.winner = determineWinner(players, communityCards);
                this.winner.addToChipCount(pot);
                pot = 0;

                // Reset the game state for the next hand.
                for (Player player : players) {
                    player.clearHand();
                }
                communityCards.clear();
                deck.reset();
                deck.shuffle();
                dealer = players.get((players.indexOf(dealer) + 1) % players.size());
                currentPlayerIndex = (players.indexOf(dealer) + 1) % players.size();
            } else {
                // Move to the next player's turn.
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
                currentPlayer = players.get(currentPlayerIndex);
            }
        }
    }

    private void updateUiToast(String str) {
        // Example of posting a message to update the UI
        handlerUI.post(new Runnable() {
            @Override
            public void run() {
                // Update the UI components here
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to start the turn timer
    private void startTurnTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Handle turn timeout here
                handleTurnTimeout();

            }
        }, turnTimeInSeconds * 1000); // Convert seconds to milliseconds
    }

    // Method to start a player's turn
    public void startPlayerTurn() {
        updateUiToast("New Player Turn");
        // Start the turn timer
        startTurnTimer();
        // Update the UI to indicate it's the player's turn
//        pokerGameView.updateUIToIndicateTurn(currentPlayerIndex);

    }

    // Method to handle turn timeouts
    private void handleTurnTimeout() {
        updateUiToast("Time out!");
        // Player's turn timed out, make a default move (e.g., fold)
//        pokerGameLogic.makeDefaultMoveForPlayer(currentPlayerIndex);
        // Proceed to the next player's turn
        advanceToNextPlayer();
    }

    // Method to advance to the next player
    private void advanceToNextPlayer() {
        // Update currentPlayerIndex to the next player
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        // Start the next player's turn
        startPlayerTurn();
    }

    public Player determineWinner(List<Player> players, List<Card> communityCards) {
        Player winningPlayer = null;
        int bestHandRank = -1;

        for (Player player : players) {
            int playerHandRank = player.compareHands(communityCards);

            if (playerHandRank > bestHandRank) {
                bestHandRank = playerHandRank;
                winningPlayer = player;
            }
        }

        return winningPlayer;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getPot() {
        return pot;
    }


    public void dealTwoHoleCardsToPlayers() {
        for (Player player : players)
            player.addCard(deck.dealCard());
    }

    public Player getWinner() {
        return this.winner;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public List getCommunityCards() {
        return communityCards;
    }

    public int getDealerPosition() {
        return this.dealerPosition;
    }

    public void incrementDealerPosition() {
        this.dealerPosition++;
    }

    public int getSmallBlind() {
        return this.smallBlind;
    }

    public int getBigBlind() {
        return this.bigBlind;
    }

    public int getCurrentBet() {
        return this.currentBet;
    }

    public Object getDealer() {
        return this.dealer;
    }


    // Unregister the BroadcastReceiver when it's no longer needed (e.g., in onDestroy)
    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(this);
    }

}
