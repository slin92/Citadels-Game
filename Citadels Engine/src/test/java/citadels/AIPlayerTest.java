package citadels;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import citadels.CardsAndDecks.DistrictCard;
import citadels.CardsAndDecks.DistrictColor;

public class AIPlayerTest {

    private AIPlayer ai;
    private TurnManager tm;
    private Game game;

    @BeforeEach
    public void setup() {
        game = new Game();
        game.setConsole(new Scanner(System.in));  // prevent blocking
        game.getPlayers().add(new HumanPlayer("P1"));
        ai = new AIPlayer("CPU 2");
        game.getPlayers().add(ai);
        tm = new TurnManager(game);
    }

    @Test
    public void testIncomeCollectionGold() {
        // Hand has 2 cards, AI should take gold
        ai.getHand().add(new DistrictCard("A", 1, DistrictColor.BLUE, ""));
        ai.getHand().add(new DistrictCard("B", 2, DistrictColor.RED, ""));

        int before = ai.getGold();

        ai.takeTurn(tm);

        assertEquals(before + 2, ai.getGold(), "AI should collect 2 gold when hand > 1");
    }

    @Test
    public void testIncomeCollectionDrawCardsFallback() {
        // Hand has 1 card, AI should draw cards instead
        ai.getHand().clear();
        ai.addGold(0);
        int before = ai.getHand().size();

        // Preload deck with cards so draw won't fail
        game.getDistrictDeck().returnCard(new DistrictCard("Temple", 1, DistrictColor.BLUE, ""));
        game.getDistrictDeck().returnCard(new DistrictCard("Prison", 2, DistrictColor.RED, ""));

        ai.takeTurn(tm);

        assertTrue(ai.getHand().size() > before, "AI should draw 2 cards if hand <= 1");
    }

    @Test
    public void testAISelectsAndBuildsBestCard() {
        ai.getHand().clear();
        ai.addGold(4);
        ai.getHand().add(new DistrictCard("Watchtower", 1, DistrictColor.RED, ""));
        ai.getHand().add(new DistrictCard("Castle", 4, DistrictColor.YELLOW, ""));  // most valuable build

        ai.takeTurn(tm);

        assertTrue(ai.getCity().stream().anyMatch(c -> c.getName().equals("Castle")), "AI should build Castle");
        assertTrue(ai.getGold() < 4, "AI should spend gold to build");
    }

    @Test
    public void testAIDoesNotBuildIfNoAffordableCard() {
        ai.getHand().clear();
        ai.addGold(1);
        ai.getHand().add(new DistrictCard("Cathedral", 5, DistrictColor.BLUE, ""));  // too expensive

        ai.takeTurn(tm);

        assertEquals(0, ai.getCity().size(), "AI should not build if cannot afford any cards");
        assertEquals(1, ai.getGold(), "Gold should remain unchanged");
    }

    @Test
    public void testDebugModePrintsHand() {
        ai.getHand().add(new DistrictCard("Manor", 3, DistrictColor.YELLOW, ""));
        game.setDebugMode(true);

        // Just ensure it doesn’t crash
        assertDoesNotThrow(() -> ai.takeTurn(tm));
    }

    @Test
    public void testFullTurnSimulationSmoke() {
        ai.getHand().clear();
        ai.getCity().clear();
        ai.addGold(2);

        // Give deck some drawable cards
        game.getDistrictDeck().returnCard(new DistrictCard("Temple", 1, DistrictColor.BLUE, ""));
        game.getDistrictDeck().returnCard(new DistrictCard("Harbor", 3, DistrictColor.GREEN, ""));
        game.getDistrictDeck().returnCard(new DistrictCard("Keep", 5, DistrictColor.RED, ""));

        // Hand with mixed affordables
        ai.getHand().add(new DistrictCard("Docks", 2, DistrictColor.GREEN, ""));
        ai.getHand().add(new DistrictCard("Market", 3, DistrictColor.GREEN, ""));

        // Just let it run
        assertDoesNotThrow(() -> ai.takeTurn(tm));
    }

}

