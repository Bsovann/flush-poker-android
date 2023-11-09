package com.example.flush_poker_android.Logic;

import java.util.ArrayList;
import java.util.List;

public class TexasHoldEmGamePracticeMode {
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

    private Player winner;

    public TexasHoldEmGamePracticeMode(){
        initializeGame();
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
    }

    public void startGame(){
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

        // 5. Start the game loop.
        //     while (players.size() > 1) { # Implement this while in the activity
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
//        }
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


    public void dealTwoHoleCardsToPlayers(){
            for (Player player : players)
                player.addCard(deck.dealCard());
    }

    public Player getWinner(){
        return this.winner;
    }

    public void addPlayer(Player player){
        this.players.add(player);
    }

    public List getCommunityCards(){
        return communityCards;
    }

    public int getDealerPosition(){
        return this.dealerPosition;
    }
    public void incrementDealerPosition(){
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
