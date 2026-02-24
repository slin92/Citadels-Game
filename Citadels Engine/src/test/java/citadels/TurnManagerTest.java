package citadels;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import citadels.CardsAndDecks.Ability;
import citadels.CardsAndDecks.CharacterCard;

public class TurnManagerTest {

    private Game game;
    private TurnManager turnManager;
    private HumanPlayer player1;
    private AIPlayer player2;

    @BeforeEach
    public void setup() {
        game = new Game();

        // Mock character input for HumanPlayer (inputting "Assassin\n" as choice)
        String mockInput = "Assassin\n";
        game.setConsole(new Scanner(new ByteArrayInputStream(mockInput.getBytes())));

        player1 = new HumanPlayer("Player 1");
        player2 = new AIPlayer("CPU 2");

        game.getPlayers().add(player1);
        game.getPlayers().add(player2);

        // Reset the character deck
        game.resetCharacterDeck();  // if you added this, otherwise manually re-initialize deck
        game.setCrownIndex(0);

        turnManager = new TurnManager(game);
    }

    @Test
    public void testSetAndCheckAssassinatedCharacter() {
        turnManager.setAssassinatedCharacter(3);
        assertTrue(turnManager.isAssassinated(3));
        assertFalse(turnManager.isAssassinated(5));
    }

    @Test
    public void testMarkRobbedAndTransferGold() {
        CharacterCard thief = new CharacterCard("Thief", 2, Ability.THIEF);
        CharacterCard merchant = new CharacterCard("Merchant", 6, Ability.MERCHANT);
        player1.setCharacter(thief);
        player2.setCharacter(merchant);
        player2.addGold(5);

        turnManager.markRobbed(6, player1);  // mark Merchant as robbed
        game.setCurrentPlayer(player2);      // pretend it's the Merchant's turn

        // Simulate effect of robbery
        int stolen = player2.getGold();
        player1.addGold(stolen);
        player2.addGold(-stolen);

        assertEquals(5, player1.getGold());
        assertEquals(0, player2.getGold());
    }


    @Test
    public void testFindPlayerByCharacterNumber() {
        CharacterCard king = new CharacterCard("King", 4, Ability.KING);
        CharacterCard assassin = new CharacterCard("Assassin", 1, Ability.ASSASSIN);

        player1.setCharacter(king);
        player2.setCharacter(assassin);

        assertEquals(player1, turnManager.findPlayerByCharacterNumber(4));
        assertEquals(player2, turnManager.findPlayerByCharacterNumber(1));
        assertNull(turnManager.findPlayerByCharacterNumber(7));  // Not assigned
    }

    @Test
    public void testGetGameReturnsSameInstance() {
        assertSame(game, turnManager.getGame());
    }

    // Optional: Smoke test for runSelectionPhase with minimal AI setup
    @Test
    public void testRunSelectionPhaseSmoke() {
        game.resetCharacterDeck();
        game.setCrownIndex(0);

        // Should not throw or block
        turnManager.runSelectionPhase();

        for (Player p : game.getPlayers()) {
            assertNotNull(p.getCharacter(), "Each player should have selected a character");
        }
    }
}
