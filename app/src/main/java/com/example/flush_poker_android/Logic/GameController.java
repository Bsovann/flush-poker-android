package com.example.flush_poker_android.Logic;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.example.flush_poker_android.Client.GameUpdateListener;
import com.example.flush_poker_android.Client.Utility.CardUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class GameController implements Runnable {
    List<BotPlayer> remainPlayers;
    private Deck deck;
    private List<BotPlayer> players;
    private List<Card> communityCards;
    private BotPlayer currentPlayer;
    private int dealerPosition;
    private int smallBlind;
    private int bigBlind;
    private int currentBet;
    private int pot;
    private BotPlayer dealer;
    private int currentPlayerIndex;
    private BotPlayer winner;
    private final Handler mainUiThread;
    private final Context gameContext;
    private ExecutorService playerThreadPool;
    private List<GameUpdateListener> listeners = new ArrayList<>();
    // Create CardFragment instance

    public GameController(List<BotPlayer> players, Handler handler, Context context, ExecutorService playerThreadPool) {
        this.players = players;
        this.mainUiThread = handler;
        this.gameContext = context;
        this.playerThreadPool = playerThreadPool;

        // Initialize game logic
        initializeGame();
    }

    @Override
    public void run() {
        while (isGameActive()) {
            startGame();
        }
        endGame();
    }
    private void endGame() {
        playerThreadPool.shutdown();
        // Notify HostActivity
        // Shut Down Thread
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private void initializeGame() {
        this.deck = new Deck();
        this.communityCards = new ArrayList<>();
        this.dealerPosition = 0;
        this.pot = 0;
    }
    public void startGame() {

        // DeckShuffle
        deck.shuffle();

        // Initial Game State
        smallBlind = 10;
        bigBlind = 20;
        currentBet = bigBlind;
        dealer = players.get(dealerPosition);

        // Determine small blind and big blind positions
        int smallBlindPosition = (dealerPosition + 1) % players.size();
        int bigBlindPosition = (dealerPosition + 2) % players.size();

        // Player in small blind position posts the small blind
        BotPlayer smallBlindPlayer = players.get(smallBlindPosition);
        postBlind(smallBlindPlayer, smallBlind);

        // Player in big blind position posts the big blind
        BotPlayer bigBlindPlayer = players.get(bigBlindPosition);
        postBlind(bigBlindPlayer, bigBlind);

        // Initialize hands
        dealTwoHoleCardsToPlayers();

        // Player, Right of Big Blind (Clockwise)
        currentPlayerIndex = (bigBlindPosition + 1) % players.size();
        currentPlayer = players.get(currentPlayerIndex);

        // Deal community cards when
        dealCommunityCards();
        updateCommunityCardsUI();

        // Start Betting Round!
        while (communityCards.size() != 5) {

            if (isBettingRoundComplete()) {
                dealCommunityCards();
                updateCommunityCardsUI();
            }
            else {
                updateToastUi(communityCards.toString());

                // Players can choose to fold, call, or raise.
                // Update the current bet, pot, and player actions accordingly.

                synchronized (currentPlayer) {
                    currentPlayer.setAvailableActions(currentBet);
                    if (!currentPlayer.hasFolded())
                        currentPlayer.notify();
                    else {
                        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
                        currentPlayer = players.get(currentPlayerIndex);
                        continue;
                    }
                }

                // Wait for the player's turn to be completed.
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                String playerChoice = currentPlayer.getPlayerAction();
                updateToastUi(currentPlayer.getName() + " " + playerChoice);

                // Process the player's action (e.g., update bets, check for folds, etc.)
                processPlayerChoice(playerChoice);

                // Move to the next player's turn.
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
                currentPlayer = players.get(currentPlayerIndex);

                this.remainPlayers = players.stream().filter(player -> player.hasFolded() != true).collect(Collectors.toList());
                updateToastUi("Remain Players: " + remainPlayers.size());

                if (remainPlayers.size() == 1)
                    break;
            }
        }
                if (determineWinner()) {
                    updateToastUi("The Winner is: " + winner.getName());
                }
    }

    // Update Card UI
    private void updateCommunityCardsUI() {
        List<Integer> imagesId = communityCards.stream()
                .map(card -> CardUtils.getCardImageResourceId(card.toString(), gameContext))
                .collect(Collectors.toList());

        notifyCardUpdate(imagesId);
    }


    // Other GameController methods...

    public void addGameUpdateListener(GameUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeGameUpdateListener(GameUpdateListener listener) {
        listeners.remove(listener);
    }

    // Call this method when there's a card update
    private void notifyCardUpdate(List<Integer> updatedCardImages) {
        for (GameUpdateListener listener : listeners) {
            listener.onCardUpdate(updatedCardImages);
        }
    }

    public synchronized boolean isGameActive() {
        // Check if the game is active
        return players.size() > 1;
    }
    private void updateToastUi(String playerChoice) {
        mainUiThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(gameContext, playerChoice, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void postBlind(BotPlayer player, int blindAmount) {
        player.decreaseChips(blindAmount);
        pot += blindAmount;
        // You can add a log or notification to inform other players about the blind being posted.
    }
    private Boolean determineWinner() {
        if (communityCards.size() == 5) {
            this.winner = determineWinner(players, communityCards);
            this.winner.addToChipCount(pot);
            pot = 0;

            // Reset the game state for the next hand.
            for (BotPlayer player : players) {
                player.clearHand();
            }
            communityCards.clear();
            deck.reset();
            deck.shuffle();
            dealerPosition++;
            return true;
        }
        return false;
    }
    private void processPlayerChoice(String playerChoice) {
        // Handle the player's choice.
        if (playerChoice.equals("Fold")) {
            // Player folds and is out of the hand.
            currentPlayer.fold();
//            players.remove(currentPlayer);
        } else if (playerChoice.equals("Call")) {
            // Player calls the current bet.
            int callAmount = currentBet - currentPlayer.getCurrentBet();
            currentPlayer.bet(callAmount);
            pot += callAmount;
        } else if (playerChoice.equals("Raise")) {
            // Player raises the current bet.
//            int raiseAmount = promptPlayerForRaiseAmount(currentPlayer, currentBet);
//            currentPlayer.bet(raiseAmount + currentBet);
//            currentBet += raiseAmount;
//            pot += currentBet;
        } else if (playerChoice.equals("Check")) {
        }
    }
    public boolean isBettingRoundComplete() {
        // Implement the logic to check if the betting round is complete.
        // You can iterate through the players and check if they have all matched the current bet.
        // Return true if the round is complete; otherwise, return false.
        for (BotPlayer player : players) {
            if (!player.actionIsDone()) {
                return false;
            }
        }
        for (BotPlayer player : players)
            player.setActionIsDone(false);
        return true;
    }
    public synchronized void dealCommunityCards() {
        if (communityCards.size() < 3) {
            communityCards.addAll(deck.draw(3));
        } else if (communityCards.size() == 3) {
            communityCards.addAll(deck.draw(1));
        } else if (communityCards.size() == 4) {
            communityCards.addAll(deck.draw(1));
        }
    }
    public BotPlayer determineWinner(List<BotPlayer> players, List<Card> communityCards) {
        BotPlayer winningPlayer = null;
        int bestHandRank = -1;

        for (BotPlayer player : players) {
            int playerHandRank = player.compareHands(communityCards);

            if (playerHandRank > bestHandRank) {
                bestHandRank = playerHandRank;
                winningPlayer = player;
            }
        }

        return winningPlayer;
    }
    public List<BotPlayer> getPlayers() {
        return players;
    }
    public BotPlayer getCurrentPlayer() {
        return currentPlayer;
    }
    public int getPot() {
        return pot;
    }
    public synchronized void dealTwoHoleCardsToPlayers() {
        for (BotPlayer player : players)
            player.addCard(deck.dealCard());
    }
    public BotPlayer getWinner() {
        return this.winner;
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
}
