package citadels;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import citadels.CardsAndDecks.DistrictCard;
import citadels.CardsAndDecks.DistrictColor;

public class GameTest {

    private Game game;
    private HumanPlayer player1;
    private AIPlayer player2;

    @BeforeEach
    public void setup() {
        game = new Game();

        player1 = new HumanPlayer("Player 1");
        player2 = new AIPlayer("CPU 2");

        game.getPlayers().add(player1);
        game.getPlayers().add(player2);

        game.setCurrentPlayer(player1);
    }

    @Test
    public void testSwapHands() {
        player1.getHand().add(new DistrictCard("Tavern", 1, DistrictColor.GREEN, ""));
        player2.getHand().add(new DistrictCard("Church", 2, DistrictColor.BLUE, ""));

        game.swapHands(player1, player2);

        assertEquals("Church", player1.getHand().get(0).getName());
        assertEquals("Tavern", player2.getHand().get(0).getName());
    }

    @Test
    public void testRedrawCards() {
        player1.getHand().add(new DistrictCard("Temple", 1, DistrictColor.BLUE, ""));
        player1.getHand().add(new DistrictCard("Castle", 4, DistrictColor.YELLOW, ""));

        int initialDeckSize = game.getDistrictDeck().size();

        List<Integer> indexesToRedraw = Arrays.asList(0, 1);
        game.redrawCards(player1, indexesToRedraw);

        assertEquals(2, player1.getHand().size());
        assertTrue(game.getDistrictDeck().size() <= initialDeckSize); // conservatively test
    }

    @Test
    public void testSaveAndLoadGame() {
        player1.addGold(5);
        player1.getCity().add(new DistrictCard("Market", 2, DistrictColor.GREEN, ""));

        String filename = "test-save.json";
        game.saveGame(filename);

        File f = new File(filename);
        assertTrue(f.exists());

        Game loadedGame = new Game();
        loadedGame.loadGame(filename);

        Player loadedPlayer = loadedGame.getPlayers().get(0);
        assertEquals(5, loadedPlayer.getGold());
        assertEquals("Market", loadedPlayer.getCity().get(0).getName());

        f.delete(); // cleanup
    }

    @Test
    public void testGameOverCheck() {
        for (int i = 0; i < 8; i++) {
            player1.getCity().add(new DistrictCard("Dummy" + i, 1, DistrictColor.GREEN, ""));
        }

        assertTrue(game.isGameOver());
    }

    
}


