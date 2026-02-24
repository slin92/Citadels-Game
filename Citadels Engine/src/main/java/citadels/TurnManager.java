package citadels;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import citadels.CardsAndDecks.Ability;

/**
 * Manages the character selection and action phases each round.
 */
public class TurnManager {
    private final Game game;
    private int assassinatedCharacter = -1;
    private int robbedCharacter = -1;
    private Player thiefPlayer;

    public TurnManager(Game game) {
        this.game = game;
    }

    /**
     * Selection phase: shuffle character deck, discard cards, then allow each
     * player to pick a character, updating the crown for next round.
     */
    public void runSelectionPhase() {
        System.out.println("\n================================");
        System.out.println("SELECTION PHASE");
        System.out.println("================================");

        List<Player> players = game.getPlayers();
        int numPlayers = players.size();

        // 1. Shuffle deck
        game.getCharacterDeck().shuffle();

        // 2. Discard one face-down
        CardsAndDecks.CharacterCard hidden = game.getCharacterDeck().drawForSelection();
        if (hidden != null) {
            System.out.println("A mystery character was removed.");
        } else {
            System.out.println("Character deck is empty - cannot draw.");
            return;
        }

        // 3. Discard 1 or 2 face-up (not King)
        int faceUpQty = (numPlayers == 4) ? 2 : (numPlayers == 5 ? 1 : 0);
        int removed = 0;

        while (removed < faceUpQty) {
            CardsAndDecks.CharacterCard c = game.getCharacterDeck().drawForSelection();
            if (c == null) {
                System.out.println("Character deck is empty - cannot draw.");
                break;
            }
            if (c.getAbility() == CardsAndDecks.Ability.KING) {
                game.getCharacterDeck().returnCardToDeck(c);
            } else {
                System.out.println(c.getName() + " was removed and placed face-up.");
                removed++;
            }
        }

        // 4. Build pick order based on crown holder
        List<Player> pickOrder = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            pickOrder.add(players.get((game.getCrownIndex() + i) % numPlayers));
        }

        // 5. Selection loop
        List<CardsAndDecks.CharacterCard> available = game.getCharacterDeck().getAvailableCards();
        for (Player p : pickOrder) {
            if (p instanceof HumanPlayer) {
                System.out.println("Choose your character. Available characters:");
                for (CardsAndDecks.CharacterCard c : available) {
                    System.out.println(" - " + c.getName());
                }

                CardsAndDecks.CharacterCard chosen = null;
                while (chosen == null) {
                    String input = game.getConsole().nextLine().trim();
                    for (CardsAndDecks.CharacterCard c : available) {
                        if (c.getName().equalsIgnoreCase(input)) {
                            chosen = c;
                            break;
                        }
                    }
                    if (chosen == null) System.out.print("> ");
                }

                game.getCharacterDeck().removeCard(chosen);
                p.setCharacter(chosen);
                System.out.println("Player 1 chose a character.");
            } else {
                if (available.isEmpty()) {
                    System.out.println("No available characters to choose from.");
                    return;
                }
                CardsAndDecks.CharacterCard chosen = available.get(new Random().nextInt(available.size()));
                game.getCharacterDeck().removeCard(chosen);
                p.setCharacter(chosen);
                System.out.println(p.getName() + " chose a character.");
            }

            if (p.getCharacter().getAbility() == CardsAndDecks.Ability.KING) {
                game.setCrownIndex(players.indexOf(p));
            }
        }

        // 6. Final discard (face-down) if 5+ players
        if (numPlayers >= 5 && !game.getCharacterDeck().getAvailableCards().isEmpty()) {
            game.getCharacterDeck().drawForSelection();  // silent discard
            System.out.println("One final character was discarded face-down.");
        }

        // Update state
        game.getCharacterDeck().setAvailableCards(available);  // technically redundant unless used elsewhere
    }


    public Player findPlayerByCharacterNumber(int num) {
        for (Player p : game.getPlayers()) {
            if (p.getCharacter().getRank() == num) return p;
        }
        return null;
    }

    /**
     * Action phase: execute each player's turn in ascending character rank order.
     */
    public void runTurnPhase() {
        System.out.println("\n================================");
        System.out.println("TURN PHASE");
        System.out.println("================================");

        for (int i = 1; i <= 8; i++) {
            Player p = findPlayerByCharacterNumber(i);
            if (p == null || p.getCharacter() == null) continue;

            String charName = p.getCharacter().getName();
            System.out.println(i + ": " + charName);
            System.out.println(p.getName() + " is the " + charName);

            // Assassin check
            if (isAssassinated(i)) {
                System.out.println(p.getName() + " was assassinated. Skipping turn.");
                continue;
            }

            // Wait for user to type 't' if this is a CPU turn
            if (!(p instanceof HumanPlayer)) {
                System.out.print("Press t to process turns\n> ");
                while (!"t".equalsIgnoreCase(game.getConsole().nextLine().trim())) {
                    System.out.print("It is not your turn. Press t to continue with other player turns.\n> ");
                }
            } else {
                System.out.println("Your turn.");
            }

            // Robbed gold transfer
            if (robbedCharacter == i) {
                System.out.println(p.getName() + " has been robbed.");
                thiefPlayer.addGold(p.getGold());
                p.addGold(-p.getGold());
            }

            game.setCurrentPlayer(p);
            p.resetBuildCount();

            if (p.getCharacter().getAbility() == Ability.ARCHITECT) {
                p.drawCards(2, game);
                System.out.println(p.getName() + " draws 2 extra cards and may build up to 3 districts.");
            }

            p.takeTurn(this);
            System.out.println();
        }
    }

    public void setAssassinatedCharacter(int characterNumber) {
        this.assassinatedCharacter = characterNumber;
    }

    public boolean isAssassinated(int characterNumber) {
        return this.assassinatedCharacter == characterNumber;
    }

    public Game getGame() {
        return game;
    }

    public void markAssassinated(int characterNumber) {
        this.assassinatedCharacter = characterNumber;
    }

    public void markRobbed(int characterNumber, Player thief) {
        this.robbedCharacter = characterNumber;
        this.thiefPlayer = thief;
    }

}