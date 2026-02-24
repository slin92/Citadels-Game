package citadels;

import java.util.Random;    

public class AIPlayer extends Player {
    public AIPlayer(String name) {
        super(name);
    }

    @Override
    public void takeTurn(TurnManager tm) {
        Game game = tm.getGame();
        game.setCurrentPlayer(this);

        // Show hand if debug mode is on
        if (game.isDebugMode()) {
            System.out.println(name + " (AI) hand:");
            for (int i = 0; i < hand.size(); i++) {
                CardsAndDecks.DistrictCard c = hand.get(i);
                System.out.println("  " + i + ". " + c.getName() + " [" + c.getColour() + " " + c.getCost() + "]");
            }
        }

        // STEP 1: Income choice (gold or cards)
        if (hand.size() <= 1) {
            drawCards(2, game);
            // Discard one randomly
            if (hand.size() > 1) {
                int discardIndex = new Random().nextInt(hand.size());
                CardsAndDecks.DistrictCard removed = hand.remove(discardIndex);
                System.out.println(name + " (AI) drew cards and discarded: " + removed.getName());
            } else {
                System.out.println(name + " (AI) drew cards but had nothing to discard.");
            }
        } else {
            addGold(2);
            System.out.println(name + " (AI) collected 2 gold.");
        }

        // STEP 2: Attempt to build best card
        CardsAndDecks.DistrictCard best = null;
        for (CardsAndDecks.DistrictCard card : hand) {
            if (card.getCost() <= gold && city.stream().noneMatch(d -> d.getName().equals(card.getName()))) {
                if (best == null || card.getCost() > best.getCost()) {
                    best = card;
                }
            }
        }

        if (best != null) {
            int idx = hand.indexOf(best);
            build(idx, game);
        } else {
            System.out.println(name + " (AI) could not build any districts.");
        }
    }

}
