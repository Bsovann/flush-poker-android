package com.example.flush_poker_android.Logic;

import java.util.ArrayList;
import java.util.List;

public class TexasHoldEmGamePracticeMode {
    private Deck deck;
    private List<Player> players;
    private List<Card> communityCards;
    private List<Player> bettingRound;
    private int dealerPosition;
    private int pot;

    public TexasHoldEmGamePracticeMode(){
        this.deck = new Deck();
        this.players = new ArrayList<>(5);
        this.communityCards = new ArrayList<>();
        this.dealerPosition = 0;

        // Start the Game

    }



    private void dealTwoHoleCardsToPlayers(){
        for(int i = 0; i < 2; i++) {
            for (Player player : players) {
                player.addCard(deck.dealCard());
            }
        }
    }

    public void addPlayer(Player player){
        this.players.add(player);
    }

    public void dealCommunityCards(){
        communityCards.add(deck.dealCard());
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

}
