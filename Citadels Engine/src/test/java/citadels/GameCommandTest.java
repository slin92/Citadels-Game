package citadels;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import citadels.CardsAndDecks.Ability;
import citadels.CardsAndDecks.CharacterCard;
import citadels.CardsAndDecks.DistrictCard;
import citadels.CardsAndDecks.DistrictColor;

public class GameCommandTest {

    private Game game;
    private HumanPlayer player;

    @BeforeEach
    public void setup() {
        game = new Game();

        // Default mock input to avoid blocking
        game.setConsole(new Scanner(new ByteArrayInputStream("end\n".getBytes())));

        player = new HumanPlayer("Tester");
        game.getPlayers().add(player);
        game.setCurrentPlayer(player);
    }

    private String captureOutput(Runnable command) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));
        try {
            command.run();
        } finally {
            System.setOut(originalOut);
        }
        return out.toString();
    }

    @Test
    public void testGoldCommandDisplaysGold() {
        player.addGold(3);
        String output = captureOutput(() -> game.handleCommand("gold"));
        assertTrue(output.toLowerCase().contains("3"), "Output should mention gold amount");
    }

    @Test
    public void testHandCommandDisplaysCard() {
        player.getHand().add(new DistrictCard("Tavern", 1, DistrictColor.GREEN, ""));
        String output = captureOutput(() -> game.handleCommand("hand"));
        assertTrue(output.contains("Tavern"), "Output should show the hand contents");
    }

    @Test
    public void testBuildCommandBuildsDistrict() {
        player.getHand().add(new DistrictCard("Watchtower", 1, DistrictColor.RED, ""));
        player.addGold(2);
        String output = captureOutput(() -> game.handleCommand("build 0"));
        assertTrue(output.toLowerCase().contains("built"), "Should confirm the district was built");
        assertEquals(1, player.getCity().size());
    }

    @Test
    public void testCitadelCommandDisplaysCity() {
        player.getCity().add(new DistrictCard("Temple", 2, DistrictColor.BLUE, ""));
        String output = captureOutput(() -> game.handleCommand("citadel"));
        assertTrue(output.contains("Temple"), "Should show city districts");
    }

    @Test
    public void testInfoCommandShowsPurpleAbility() {
        player.getHand().add(new DistrictCard("Haunted City", 2, DistrictColor.PURPLE, "Counts as any type"));
        String output = captureOutput(() -> game.handleCommand("info 0"));
        assertTrue(output.toLowerCase().contains("counts as any"), "Should show purple district ability text");
    }

    @Test
    public void testInvalidCommandPrintsHelp() {
        String output = captureOutput(() -> game.handleCommand("notacommand"));
        assertTrue(output.toLowerCase().contains("available commands"), "Should provide help or usage output");
    }

    @Test
    public void testEndCommandPrintsConfirmation() {
        String output = captureOutput(() -> game.handleCommand("end"));
        assertTrue(output.toLowerCase().contains("ended your turn"), "Should confirm turn end");
    }

    @Test
    public void testActionCommandMagicianSwapHelp() {
        player.setCharacter(new CharacterCard("Magician", 3, Ability.MAGICIAN));
        game.setConsole(new Scanner(new ByteArrayInputStream("action\n".getBytes())));
        String output = captureOutput(() -> game.handleCommand("action"));
        assertTrue(output.toLowerCase().contains("swap") || output.toLowerCase().contains("redraw"),
            "Should provide Magician action instructions");
    }
}

