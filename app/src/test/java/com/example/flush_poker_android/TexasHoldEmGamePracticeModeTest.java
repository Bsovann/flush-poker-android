package com.example.flush_poker_android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.flush_poker_android.Logic.PracticeModeGameController;
import com.example.flush_poker_android.Logic.Player;

import org.junit.Before;
import org.junit.Test;

public class TexasHoldEmGamePracticeModeTest {

    private PracticeModeGameController game;

    @Before
    public void setUp() throws InterruptedException {
//        game = new TexasHoldEmGamePracticeMode();
        game.startGame();
    }

    @Test
    public void testInitialization() {
        assertNotNull(game.getPlayers());
        assertEquals(5, game.getPlayers().size());
        assertEquals(10, game.getSmallBlind());
        assertEquals(20, game.getBigBlind());
        assertEquals(20, game.getCurrentBet());
        assertEquals(0, game.getPot());
        assertNotNull(game.getDealer());
    }

    @Test
    public void testDealHoleCardsToPlayers() {
        game.dealTwoHoleCardsToPlayers();
        for (Player player : game.getPlayers()) {
            assertEquals(2, player.getHand().size());
        }
    }

    @Test
    public void testIncrementDealerPosition() {
        int initialPosition = game.getDealerPosition();
        game.incrementDealerPosition();
        assertEquals(initialPosition + 1, game.getDealerPosition());
    }

    @Test
    public void testGetPlayers() {
        assertNotNull(game.getPlayers());
        assertEquals(5, game.getPlayers().size());
    }

    @Test
    public void testGetCurrentPlayer() {
        Player currentPlayer = game.getCurrentPlayer();
        assertNotNull(currentPlayer);
        assertTrue(game.getPlayers().contains(currentPlayer));
    }

    @Test
    public void testGetPot() {
        assertEquals(0, game.getPot());
    }

    // Implement Later After Tested on Hand.
    @Test
    public void testDeterminingWinner() {
        // Create a test scenario with known hands and community cards

        // and verify that the correct winner is determined.
        // You will need to customize this test based on your game logic.
        // Example:
        // Player winner = game.determineWinner(players, communityCards);
//         assertNotNull(game.getWinner());

    }
}

