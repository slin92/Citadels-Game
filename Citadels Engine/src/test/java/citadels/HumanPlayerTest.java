package citadels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import citadels.CardsAndDecks.DistrictCard;
import citadels.CardsAndDecks.DistrictColor;

public class HumanPlayerTest {

    private HumanPlayer player;
    private Game dummyGame;
    private DistrictCard tavern;
    private DistrictCard cathedral;

    @BeforeEach
    public void setup() {
        player = new HumanPlayer("TestPlayer");
        dummyGame = new Game();
        tavern = new DistrictCard("Tavern", 1, DistrictColor.GREEN, "");
        cathedral = new DistrictCard("Cathedral", 5, DistrictColor.BLUE, "");
    }

    @Test
    public void testPlayerNameAndGold() {
        assertEquals("TestPlayer", player.getName());
        player.addGold(4);
        assertEquals(4, player.getGold(), "Gold should be correctly added and retrieved");
    }

    @Test
    public void testBuildDistrictSuccess() {
        player.getHand().add(cathedral);
        player.addGold(5);
        boolean built = player.buildDistrict(0, dummyGame);
        assertTrue(built);
        assertEquals(0, player.getGold());
        assertEquals(1, player.getCity().size());
    }

    @Test
    public void testBuildDistrictFailsWithoutEnoughGold() {
        player.getHand().add(cathedral);
        player.addGold(3); // Not enough
        boolean built = player.buildDistrict(0, dummyGame);
        assertFalse(built, "Build should fail due to insufficient gold");
        assertEquals(1, player.getHand().size());
        assertEquals(0, player.getCity().size());
    }

    @Test
    public void testGainGoldFromDistrictColor() {
        player.getCity().add(new DistrictCard("Temple", 1, DistrictColor.BLUE, ""));
        player.getCity().add(new DistrictCard("Church", 2, DistrictColor.BLUE, ""));
        player.gainGoldFromDistrict(DistrictColor.BLUE);
        assertEquals(2, player.getGold(), "Should gain gold from each matching BLUE district");
    }

    @Test
    public void testResetBuildCount() {
        // These methods must exist in Player.java:
        // public int getBuildCount()
        // public void incrementBuildCount()

        player.incrementBuildCount();
        player.incrementBuildCount();
        assertEquals(2, player.getBuildCount(), "Build count should track increments");

        player.resetBuildCount();
        assertEquals(0, player.getBuildCount(), "Reset should zero out build count");
    }
}
