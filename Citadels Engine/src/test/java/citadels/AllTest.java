package citadels;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.jupiter.api.Test;
//import org.junit.Test;

public class AllTest {

    @Test
    public void testDistrictCardCreation() {
        CardsAndDecks.DistrictCard card = new CardsAndDecks.DistrictCard("Castle", 4, CardsAndDecks.DistrictColor.YELLOW, "A stronghold");
        assertEquals("Castle", card.getName());
        assertEquals(4, card.getCost());
        assertEquals(CardsAndDecks.DistrictColor.YELLOW, card.getColour());
        assertEquals("A stronghold", card.getText());
    }

    @Test
    public void testCharacterCardCreation() {
        CardsAndDecks.CharacterCard card = new CardsAndDecks.CharacterCard("King", 4, CardsAndDecks.Ability.KING);
        assertEquals("King", card.getName());
        assertEquals(4, card.getRank());
        assertEquals(CardsAndDecks.Ability.KING, card.getAbility());
    }

    @Test
    public void testPlayerBuild() {
        Game game = new Game();
        Player p = new HumanPlayer("Tester");
        CardsAndDecks.DistrictCard card = new CardsAndDecks.DistrictCard("Temple", 1, CardsAndDecks.DistrictColor.BLUE, "");

        p.getHand().add(card);
        p.addGold(5);
        boolean result = p.build(0, game);

        assertTrue(result);
        assertEquals(1, p.getCity().size());
        assertEquals(4, p.getGold());
    }

    @Test
    public void testDuplicateDistrictBuildFails() {
        Game game = new Game();
        Player p = new HumanPlayer("Tester");
        CardsAndDecks.DistrictCard card = new CardsAndDecks.DistrictCard("Temple", 1, CardsAndDecks.DistrictColor.BLUE, "");

        p.getHand().add(card);
        p.getCity().add(new CardsAndDecks.DistrictCard("Temple", 1, CardsAndDecks.DistrictColor.BLUE, ""));
        p.addGold(5);
        boolean result = p.build(0, game);

        assertFalse(result);
    }

    @Test
    public void testDistrictDeckDrawAndShuffle() {
        CardsAndDecks.DistrictDeck deck = new CardsAndDecks.DistrictDeck();
        int originalSize = deck.size();
        deck.shuffle();
        CardsAndDecks.DistrictCard drawn = deck.draw();

        assertNotNull(drawn);
        assertEquals(originalSize - 1, deck.size());
    }

    @Test
    public void testSwapHands() {
        Game game = new Game();
        Player a = new HumanPlayer("A");
        Player b = new HumanPlayer("B");

        a.getHand().add(new CardsAndDecks.DistrictCard("Temple", 1, CardsAndDecks.DistrictColor.BLUE, ""));
        b.getHand().add(new CardsAndDecks.DistrictCard("Castle", 4, CardsAndDecks.DistrictColor.YELLOW, ""));

        game.swapHands(a, b);

        assertEquals("Castle", a.getHand().get(0).getName());
        assertEquals("Temple", b.getHand().get(0).getName());
    }

    @Test
    public void testRedrawCards() {
        Game game = new Game();
        Player p = new HumanPlayer("P");
        CardsAndDecks.DistrictCard card1 = new CardsAndDecks.DistrictCard("Temple", 1, CardsAndDecks.DistrictColor.BLUE, "");
        CardsAndDecks.DistrictCard card2 = new CardsAndDecks.DistrictCard("Castle", 4, CardsAndDecks.DistrictColor.YELLOW, "");
        p.getHand().add(card1);
        p.getHand().add(card2);

        List<Integer> indexes = Arrays.asList(0, 1);
        game.redrawCards(p, indexes);

        assertEquals(2, p.getHand().size()); // Should have replaced both cards
    }

    @Test
    public void testSaveAndLoadGame() {
        Game game = new Game();
        Player p = new HumanPlayer("Player 1");
        p.addGold(4);
        CardsAndDecks.DistrictCard c = new CardsAndDecks.DistrictCard("Temple", 1, CardsAndDecks.DistrictColor.BLUE, "");
        p.getHand().add(c);
        game.getPlayers().add(p);
        game.saveGame("testgame.json");

        Game loaded = new Game();
        loaded.loadGame("testgame.json");

        Player loadedP = loaded.getPlayers().get(0);
        assertEquals("Player 1", loadedP.getName());
        assertEquals(4, loadedP.getGold());
        assertEquals("Temple", loadedP.getHand().get(0).getName());
    }
}
