package citadels;

public class HumanPlayer extends  Player{
    public HumanPlayer(String name){
        super(name);
    }

    @Override
    public void takeTurn(TurnManager tm) {
        Game game = tm.getGame();
        game.setCurrentPlayer(this);

        // STEP 1: Prompt for income choice
        System.out.println("Do you want to take 2 gold or draw 2 cards? (gold/cards)");
        String choice = game.getConsole().nextLine().trim().toLowerCase();
        if (choice.equals("gold")) {
            addGold(2);
            System.out.println("You collected 2 gold.");
        } else if (choice.equals("cards")) {
            drawCards(2, game);
            System.out.println("You drew 2 cards:");
            showHand();

            // Prompt player to discard one
            System.out.println("Enter the index of the card you want to discard:");
            int discardIndex = -1;
            while (true) {
                try {
                    discardIndex = Integer.parseInt(game.getConsole().nextLine().trim());
                    if (discardIndex >= 0 && discardIndex < hand.size()) break;
                } catch (NumberFormatException ignored) {}
                System.out.println("Invalid input. Please enter a valid card index:");
            }

            CardsAndDecks.DistrictCard removed = hand.remove(discardIndex);
            System.out.println("You discarded: " + removed.getName());
        } else {
            System.out.println("Invalid input. You receive 2 gold by default.");
            addGold(2);
        }

        // STEP 2: Command loop for build/action/etc.
        boolean turnEnded = false;
        while (!turnEnded) {
            String input = game.getConsole().nextLine().trim();
            if (input.equalsIgnoreCase("end")) {
                System.out.println("You ended your turn.");
                turnEnded = true;
            } else {
                game.handleCommand(input);
            }
        }
    }

}
