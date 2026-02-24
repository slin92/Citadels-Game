package citadels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlayerTest {

    private Player player;
    private Game game;
    private CardsAndDecks.DistrictCard tavern;
    private CardsAndDecks.DistrictCard cathedral;
    private CardsAndDecks.DistrictCard duplicateTavern;

    @BeforeEach
    public void setup() {
        player = new HumanPlayer("TestPlayer");
        game = new Game();

        tavern = new CardsAndDecks.DistrictCard("Tavern", 1, CardsAndDecks.DistrictColor.GREEN, "");
        cathedral = new CardsAndDecks.DistrictCard("Cathedral", 5, CardsAndDecks.DistrictColor.BLUE, "");
        duplicateTavern = new CardsAndDecks.DistrictCard("Tavern", 1, CardsAndDecks.DistrictColor.GREEN, "");
    }

    @Test
    public void testAddAndSpendGold() {
        player.addGold(5);
        assertEquals(5, player.getGold());

        player.addGold(-2);
        assertEquals(3, player.getGold());
    }

    @Test
    public void testBuildDistrictSuccess() {
        player.getHand().add(cathedral);
        player.addGold(5);
        boolean built = player.buildDistrict(0, game);
        assertTrue(built, "Should build district if enough gold");
        assertEquals(1, player.getCity().size());
        assertEquals(0, player.getGold());
    }

    @Test
    public void testBuildDistrictFailsIfDuplicate() {
        player.getHand().add(duplicateTavern);
        player.getCity().add(tavern);
        player.addGold(2);
        assertFalse(player.buildDistrict(0, game), "Should not build duplicate district");
    }

    @Test
    public void testBuildDistrictFailsIfNotEnoughGold() {
        player.getHand().add(cathedral);
        player.addGold(2); // less than 5
        boolean built = player.buildDistrict(0, game);
        assertFalse(built, "Should fail to build due to insufficient gold");
        assertEquals(1, player.getHand().size());
        assertEquals(0, player.getCity().size());
    }

    @Test
    public void testGainGoldFromDistrictColor() {
        player.getCity().add(new CardsAndDecks.DistrictCard("Church", 2, CardsAndDecks.DistrictColor.BLUE, ""));
        player.getCity().add(new CardsAndDecks.DistrictCard("Temple", 1, CardsAndDecks.DistrictColor.BLUE, ""));
        player.gainGoldFromDistrict(CardsAndDecks.DistrictColor.BLUE);
        assertEquals(2, player.getGold(), "Should gain gold from matching district color");
    }

    @Test
    public void testHasDistrict() {
        player.getCity().add(tavern);
        assertTrue(player.hasDistrict("Tavern"));
        assertFalse(player.hasDistrict("Temple"));
    }

    @Test
    public void testScoreCalculationManual() {
        player.getCity().add(tavern);     // 1 gold
        player.getCity().add(cathedral);  // 5 gold
        int score = player.getCity().stream().mapToInt(CardsAndDecks.DistrictCard::getCost).sum();
        assertEquals(6, score, "Score should be sum of district costs");
    }


    @Test
    public void testDrawCardAddsToHand() {
        assertEquals(0, player.getHand().size());
        player.getHand().add(tavern);
        assertEquals(1, player.getHand().size());
        assertEquals("Tavern", player.getHand().get(0).getName());
    }
}


