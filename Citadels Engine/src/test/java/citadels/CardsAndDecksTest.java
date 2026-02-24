package citadels;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CardsAndDecksTest {

    private CardsAndDecks.DistrictDeck districtDeck;
    private CardsAndDecks.CharacterDeck characterDeck;

    @BeforeEach
    public void setup() {
        districtDeck = new CardsAndDecks.DistrictDeck();
        characterDeck = new CardsAndDecks.CharacterDeck();
    }

    @Test
    public void testDistrictDeckNotEmptyAfterInit() {
        assertTrue(districtDeck.size() > 0, "District deck should be populated.");
    }

    @Test
    public void testCharacterDeckHasEightCharacters() {
        assertEquals(8, characterDeck.getAvailableCards().size(), "Character deck should contain 8 characters.");
    }

    @Test
    public void testDrawDistrictCardReducesDeckSize() {
        int initialSize = districtDeck.size();
        CardsAndDecks.DistrictCard card = districtDeck.draw();
        assertNotNull(card, "Drawn card should not be null.");
        assertEquals(initialSize - 1, districtDeck.size(), "Deck size should reduce after drawing a card.");
    }

    @Test
    public void testReturnDistrictCardIncreasesDeckSize() {
        CardsAndDecks.DistrictCard card = districtDeck.draw();
        int sizeAfterDraw = districtDeck.size();
        districtDeck.returnCard(card);
        assertEquals(sizeAfterDraw + 1, districtDeck.size(), "Deck size should increase after returning a card.");
    }

    @Test
    public void testFindDistrictCardByNameReturnsCorrectCard() {
        CardsAndDecks.DistrictCard found = districtDeck.findByName("Temple");
        assertNotNull(found, "Card 'Temple' should exist in the deck.");
        assertEquals("Temple", found.getName(), "Card name should match.");
    }

    @Test
    public void testDrawAllCharactersThenDeckIsEmpty() {
        for (int i = 0; i < 8; i++) {
            CardsAndDecks.CharacterCard card = characterDeck.drawForSelection();
            assertNotNull(card, "Each drawn character card should not be null.");
        }
        assertEquals(0, characterDeck.getAvailableCards().size(), "Deck should be empty after drawing all cards.");
    }

    @Test
    public void testReturnCharacterCardToDeck() {
        CardsAndDecks.CharacterCard card = characterDeck.drawForSelection();
        int sizeBefore = characterDeck.getAvailableCards().size();
        characterDeck.returnCardToDeck(card);
        assertEquals(sizeBefore + 1, characterDeck.getAvailableCards().size(), "Deck size should increase after returning a character card.");
    }

    @Test
    public void testCharacterCardToStringFormat() {
        CardsAndDecks.CharacterCard card = new CardsAndDecks.CharacterCard("King", 4, CardsAndDecks.Ability.KING);
        String repr = card.toString();
        assertTrue(repr.contains("King") && repr.contains("Rank 4"), "Character card toString should contain name and rank.");
    }

    @Test
    public void testDistrictCardToStringFormat() {
        CardsAndDecks.DistrictCard card = new CardsAndDecks.DistrictCard("Temple", 1, CardsAndDecks.DistrictColor.BLUE, "A place of worship.");
        String repr = card.toString();
        assertTrue(repr.contains("Temple") && repr.contains("cost 1") && repr.contains("BLUE"), "District card toString should contain name, cost and color.");
    }

    @Test
    public void testShuffleDistrictDeckDoesNotThrow() {
        assertDoesNotThrow(() -> districtDeck.shuffle(), "Shuffling should not throw an exception.");
    }

    @Test
    public void testRemoveCharacterCard() {
        CardsAndDecks.CharacterCard card = characterDeck.getAvailableCards().get(0);
        characterDeck.removeCard(card);
        assertFalse(characterDeck.getAvailableCards().contains(card), "Card should be removed from the deck.");
    }

    @Test
    public void testDrawAtReturnsCardAndRemovesIt() {
        int originalSize = characterDeck.getAvailableCards().size();
        CardsAndDecks.CharacterCard card = characterDeck.drawAt(0);
        assertNotNull(card);
        assertEquals(originalSize - 1, characterDeck.getAvailableCards().size());
    }


}
