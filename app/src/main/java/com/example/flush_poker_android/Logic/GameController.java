package com.example.flush_poker_android.Logic;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flush_poker_android.Logic.Utility.CardUtils;
import com.example.flush_poker_android.network.PlayerStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class GameController extends AppCompatActivity implements Runnable {
    List<Player> remainPlayers;
    private Deck deck;
    private List<Card> communityCards;
    private Player currentPlayer;
    private int dealerPosition;
    private int smallBlind;
    private int bigBlind;
    private int currentBet;
    private int pot;
    private Player dealer;
    private int currentPlayerIndex;
    private Player winner;
    private final Handler mainUiThread;
    private final Context gameContext;
    private ExecutorService playerThreadPool;
    private List<Integer> communityCardsId;
    private static Semaphore remainingSeats;
    private static final int COMMUNITY_CARDS_MSG = 1, REMAIN_PLAYERS_MSG = 2, DEALER_INDEX_MSG = 3, WINNER_MSG = 4, POT_MSG = 5, PLAYER_INDEX_MSG = 6, CURRENT_PLAYER_ACTION_MSG = 7;
    private ServerSocket serverSocket;
    private Socket clientSocket; // list of client sockets to write to
    private ArrayList<Player> players = new ArrayList<>(5);
    private ArrayList<PlayerStream> playerStreams = new ArrayList<>(5);


    public GameController(Handler handler, Context context, ExecutorService playerThreadPool, Semaphore remainingSeats) {
        this.mainUiThread = handler;
        this.gameContext = context;
        this.playerThreadPool = playerThreadPool;
        this.remainingSeats = remainingSeats;
        initializeGame();
    }

    private void serverListener(){
        try {
            serverSocket = new ServerSocket(4096);
            Log.d("Networking", "Server Socket Opened");
        } catch (IOException e) {throw new RuntimeException(e);}
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            ExecutorService clientService = Executors.newFixedThreadPool(5);
            int numPlayers = 0;
            // Listen for connections
            while (numPlayers < 5) {
                try {
                    clientSocket = serverSocket.accept();
                    clientService.execute(() -> {
                        try {
                            playerStreams.add(new PlayerStream((ObjectInputStream) clientSocket.getInputStream(),(ObjectOutputStream)clientSocket.getOutputStream()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    Log.d("Networking", "Socket Docking Achieved Bon Bon!");
                } catch (IOException e) {throw new RuntimeException(e);}
            }
        });
    }

    @Override
    public void run() {
       this.serverListener();
//        if(playerStreams != null) {
//            while (isGameActive()) {
//                startGame();
//            }
//        }
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

        //dealer = playerStreams.get(dealerPosition);
        notifyDealerPositionUpdateToActivity();

        // Determine small blind and big blind positions
        int smallBlindPosition = (dealerPosition + 1) % playerStreams.size();
        int bigBlindPosition = (dealerPosition + 2) % playerStreams.size();

        // Player in small blind position posts the small blind
        Player smallBlindPlayer = players.get(smallBlindPosition);
        postBlind(smallBlindPlayer, smallBlind);

        // Player in big blind position posts the big blind
        Player bigBlindPlayer = players.get(bigBlindPosition);
        postBlind(bigBlindPlayer, bigBlind);

        // Initialize hands
        dealTwoCardsToPlayers();

        // Player, Right of Big Blind (Clockwise)
        currentPlayerIndex = (bigBlindPosition + 1) % players.size();
        currentPlayer = players.get(currentPlayerIndex);

        // Deal community cards when
        dealCommunityCards();

        // Start Betting Round!
        while (communityCards.size() != 5) {

            if (isBettingRoundComplete()) {
                dealCommunityCards();
            }
            else {
                notifyCurrentPlayerUpdateToActivity();
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
                        //player.connectToService()
                        //in player class, when server connected, notify ui
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                String playerChoice = currentPlayer.getPlayerAction();
                // Process the player's action (e.g., update bets, check for folds, etc.)
                processPlayerChoice(playerChoice);
                notifyPotUpdateToActivity();
                notifyCurrentPlayerActionToActivity();

                // Move to the next player's turn.
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
                currentPlayer = players.get(currentPlayerIndex);

                this.remainPlayers = players.stream().filter(player -> player.hasFolded() == false).collect(Collectors.toList());
                notifyRemainPlayersUpdateToActivity(); // set Remain players and compare hands.

                if (remainPlayers.size() == 1) {
                    break;
                }
            }
        }

        if(determineWinner()){
            notifyWinnerUpdateToActivity();
            updateToastUi("The Winner is " + winner.getName());
        }

        synchronized (this){
            try {
                this.wait();
                gameStateReset();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        updateToastUi("New Game!!");
    }

    private void notifyCurrentPlayerActionToActivity() {
        Message message = mainUiThread.obtainMessage();
        message.what = this.CURRENT_PLAYER_ACTION_MSG;

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", currentPlayer.getPlayerAction());
        message.setData(bundle);
        mainUiThread.sendMessage(message);
    }

    private void setRemainPlayers(){

    }
    // Update Card UI
    private void updateCommunityCardsUI(List<Card> images) {
        this.communityCardsId = images.stream()
                .map(card -> CardUtils.getCardImageResourceId(card.toString(), gameContext))
                .collect(Collectors.toList());
        notifyCommunityCardsUpdateToActivity(communityCardsId);
    }

    // Call this method when there's a card update
    private void notifyCommunityCardsUpdateToActivity(List<Integer> cardIds) {
        Message message = mainUiThread.obtainMessage();
        message.what = this.COMMUNITY_CARDS_MSG;

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", (Serializable) cardIds);
        message.setData(bundle);
        mainUiThread.sendMessage(message);
    }
    private void notifyPotUpdateToActivity() {
        Message message = mainUiThread.obtainMessage();
        message.what = this.POT_MSG;

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", (Serializable) this.pot);
        message.setData(bundle);
        mainUiThread.sendMessage(message);
    }

    private void notifyWinnerUpdateToActivity() {
        Message message = mainUiThread.obtainMessage();
        message.what = this.WINNER_MSG;

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", (Serializable) this.winner);
        message.setData(bundle);
        mainUiThread.sendMessage(message);
    }
    private void notifyRemainPlayersUpdateToActivity() {
        Message message = mainUiThread.obtainMessage();
        message.what = this.REMAIN_PLAYERS_MSG;

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", (Serializable) this.remainPlayers);
        message.setData(bundle);
        mainUiThread.sendMessage(message);
    }
    private void notifyCurrentPlayerUpdateToActivity() {
        Message message = mainUiThread.obtainMessage();
        message.what = this.PLAYER_INDEX_MSG;

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", (Serializable) this.currentPlayerIndex);
        message.setData(bundle);
        mainUiThread.sendMessage(message);
    }
    private void notifyDealerPositionUpdateToActivity() {
        Message message = mainUiThread.obtainMessage();
        message.what = this.DEALER_INDEX_MSG;

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", (Serializable) this.dealerPosition);
        message.setData(bundle);
        mainUiThread.sendMessage(message);
    }
    public synchronized boolean isGameActive() {
        // Check if the game is active
        return playerStreams.size() > 1;
    }
    private void updateToastUi(String playerChoice) {
        mainUiThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(gameContext, playerChoice, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void gameStateReset(){
        // Reset the game state for the next hand.
        for (Player player : players) {
            player.playerStateReset();
        }

        remainPlayers = players;
        communityCards.clear();
        communityCardsId.clear();
        deck.reset();
        deck.shuffle();
        dealerPosition = (dealerPosition + 1) % players.size();

        pot = 0;
    }
    private void postBlind(Player player, int blindAmount) {
        player.decreaseChips(blindAmount);
        pot += blindAmount;
        notifyPotUpdateToActivity();
    }
    private Boolean determineWinner() {
        if (communityCards.size() == 5 || remainPlayers.size() == 1) {
            this.winner = determineWinner(remainPlayers, communityCards);
            this.winner.addToChipCount(pot);
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
    public synchronized boolean isBettingRoundComplete() {
        // Implement the logic to check if the betting round is complete.
        // You can iterate through the players and check if they have all matched the current bet.
        // Return true if the round is complete; otherwise, return false.
        for (Player player : remainPlayers) {
            if (!player.actionIsDone()) {
                return false;
            }
        }
        for (Player player : remainPlayers)
            player.setActionIsDone(false);
        return true;
    }
    public synchronized void dealCommunityCards() {
        if (communityCards.size() < 3) {
            List<Card> cards = deck.draw(3);
            communityCards.addAll(cards);
            updateCommunityCardsUI(cards);
        } else if (communityCards.size() == 3) {
            List<Card> cards = deck.draw(1);
            communityCards.addAll(cards);
            updateCommunityCardsUI(cards);
        } else if (communityCards.size() == 4) {
            List<Card> cards = deck.draw(1);
            communityCards.addAll(cards);
            updateCommunityCardsUI(cards);
        }
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
    public synchronized void dealTwoCardsToPlayers() {
        for(int i = 0; i < 2; i++) {
            for (Player player : remainPlayers)
                player.addCard(deck.dealCard());
        }
    }
    public Player getWinner() {
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
