package citadels;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Player{
    protected final String name;
    protected int gold = 2;
    protected List<CardsAndDecks.DistrictCard> hand = new ArrayList<>();
    protected List<CardsAndDecks.DistrictCard> city = new ArrayList<>();
    protected CardsAndDecks.CharacterCard character;
    private int buildsThisTurn = 0;
    protected int buildCount;


    public Player(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public int getGold(){
        return gold;
    }

    public List<CardsAndDecks.DistrictCard> getHand() {
        return hand; 
    }

    public List<CardsAndDecks.DistrictCard> getCity() { 
        return city; 
    }

    public CardsAndDecks.CharacterCard getCharacter() { 
        return character; 
    }

    public void setCharacter(CardsAndDecks.CharacterCard c) { 
        this.character = c; 
    }

    public abstract void takeTurn(TurnManager tm);

    public void drawDistricts(CardsAndDecks.DistrictDeck deck, int n) {
        for (int i = 0; i < n; i++) hand.add(deck.draw());
    }

    public boolean hasDistrict(String name) {
        return city.stream().anyMatch(d -> d.getName().equalsIgnoreCase(name));
    }


    public boolean build(int handIndex, Game game) {
        int maxBuilds = (character != null && character.getAbility() == CardsAndDecks.Ability.ARCHITECT) ? 3 : 1;
        if (buildsThisTurn >= maxBuilds) return false;

        if (handIndex < 0 || handIndex >= hand.size()) return false;

        CardsAndDecks.DistrictCard card = hand.get(handIndex);
        if (gold < card.getCost() || city.stream().anyMatch(d -> d.getName().equals(card.getName()))) return false;

        gold -= card.getCost();
        city.add(card);
        hand.remove(handIndex);
        buildsThisTurn++;
        System.out.println("You built " + card.getName() + " [" + card.getColour().name().toLowerCase() + card.getCost() + "]");
        if (city.size() >= 8 && game.getFirstFinisher() == null) {
            game.setFirstFinisher(this);
        }
        return true;
    }

    public void performSpecialAction(Game game, String[] tokens) {
        CardsAndDecks.Ability ability = character.getAbility();
        boolean isAI = !(this instanceof HumanPlayer);

        switch (ability) {
            case ASSASSIN: {
                int target = isAI ? pickRandomTarget(2, 8)
                                : getTargetFromTokens(game, tokens, 2, 8);
                game.getTurnManager().markAssassinated(target);
                String name = game.getCharacterDeck().getNameByRank(target);
                System.out.println(getName() + " assassinated the " + name + ".");
                break;
            }

            case THIEF: {
                int robTarget = isAI ? pickRandomTarget(3, 8)
                                    : getTargetFromTokens(game, tokens, 3, 8);
                game.getTurnManager().markRobbed(robTarget, this);
                String characterName = game.getCharacterDeck().getNameByRank(robTarget);
                System.out.println(getName() + " robbed the " + characterName + ".");
                break;
            }

            case MAGICIAN: {
                if (isAI) {
                    if (new Random().nextBoolean()) {
                        // Redraw all cards
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < hand.size(); i++) {
                            sb.append(i).append(",");
                        }
                        game.redrawCards(this, sb.toString());
                        System.out.println(getName() + " (AI) redraws their hand.");
                    } else {
                        // Swap with random other player
                        List<Player> others = new ArrayList<>();
                        for (Player p : game.getPlayers()) {
                            if (p != this) {
                                others.add(p);
                            }
                        }
                        Player swapTarget = others.get(new Random().nextInt(others.size()));
                        game.swapHands(this, swapTarget);
                        System.out.println(getName() + " (AI) swaps hands with " + swapTarget.getName());
                    }
                } else {
                    if (tokens.length > 1 && tokens[1].equalsIgnoreCase("swap") && tokens.length > 2) {
                        try {
                            int targetIndex = Integer.parseInt(tokens[2]) - 1;
                            if (targetIndex >= 0 && targetIndex < game.getPlayers().size()) {
                                game.swapHands(this, game.getPlayers().get(targetIndex));
                                System.out.println("You swapped hands with " + game.getPlayers().get(targetIndex).getName());
                            } else {
                                System.out.println("Invalid player number.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid player number.");
                        }
                    } else if (tokens.length > 1 && tokens[1].equalsIgnoreCase("redraw") && tokens.length > 2) {
                        String input = tokens[2];  // "0,1" format
                        game.redrawCards(this, input);
                        System.out.println("You redrew some cards.");
                    } else {
                        // Fallback to interactive prompt
                        System.out.println("Choose: 1. Swap hands  2. Redraw");
                        int choice = game.getConsole().nextInt();
                        game.getConsole().nextLine();
                        if (choice == 1) {
                            System.out.println("Enter player number to swap with:");
                            int swapPlayer = game.getConsole().nextInt();
                            game.swapHands(this, game.getPlayers().get(swapPlayer - 1));
                        } else {
                            System.out.println("Enter indexes to discard (comma-separated):");
                            String input = game.getConsole().nextLine(); 
                            game.redrawCards(this, input);
                        }
                    }
                }
                break;
            }

            case KING:
                gainGoldFromDistrict(CardsAndDecks.DistrictColor.YELLOW);
                game.setCrownPlayer(this);
                break;

            case BISHOP:
                gainGoldFromDistrict(CardsAndDecks.DistrictColor.BLUE);
                break;

            case MERCHANT:
                gainGoldFromDistrict(CardsAndDecks.DistrictColor.GREEN);
                gold += 1;
                System.out.println(getName() + " receives +1 gold from Merchant ability.");
                break;

            case ARCHITECT:
                drawCards(2, game);
                break;

            case WARLORD:
                gainGoldFromDistrict(CardsAndDecks.DistrictColor.RED);
                game.handleWarlordAction(this);
                System.out.println(getName() + " receives +1 gold from Warlord ability.");
                break;
        }
    }


    public void addGold(int amount) { gold += amount; }

    public void gainGoldFromDistrict(CardsAndDecks.DistrictColor color) {
        int bonus = 0;
        for (CardsAndDecks.DistrictCard d : city) {
            if (d.getColour() == color ||
                (d.getColour() == CardsAndDecks.DistrictColor.PURPLE &&
                d.getName().equalsIgnoreCase("School of Magic"))) {
                bonus++;
            }
        }
        gold += bonus;
        System.out.println(name + " gains " + bonus + " gold from " + color + " districts.");
    }


    public void drawCards(int n, Game game) {
        drawDistricts(game.getDistrictDeck(), n);
    }

    public void showHand() {
        System.out.println("Your hand:");
        for (int i = 0; i < hand.size(); i++) {
            CardsAndDecks.DistrictCard card = hand.get(i);
            System.out.println(i + ". " + card.getName() + " [" + card.getColour().name().toLowerCase() + card.getCost() + "]");
        }
    }

    public void showGold() {
        System.out.println("You have " + gold + " gold.");
    }

    public void showCity() {
        System.out.println(getName() + " has built:");
        for (CardsAndDecks.DistrictCard card : city) {
            System.out.println(card.getName() + " (" + card.getColour().name().toLowerCase() + "), points: " + card.getCost());
        }
    }

    public void endTurn() {
        System.out.println("You ended your turn.");
    }

    public boolean buildDistrict(int index, Game game) {
        return build(index, game); // already implemented
    }

    public void showInfo(String target) {
        try {
            int index = Integer.parseInt(target);
            if (index >= 0 && index < hand.size()) {
                CardsAndDecks.DistrictCard card = hand.get(index);
                System.out.println("Card: " + card.getName());
                System.out.println("Cost: " + card.getCost());
                System.out.println("Color: " + card.getColour());
                System.out.println("Text: " + card.getText());
                return;
            } else {
                System.out.println("Invalid card index.");
                return;
            }
        } catch (NumberFormatException e) {
            // Not a number, maybe it's a character name
        }

        try {
            CardsAndDecks.Ability ability = CardsAndDecks.Ability.valueOf(target.toUpperCase());
            System.out.println("Character: " + ability);
            switch (ability) {
                case ASSASSIN:
                    System.out.println("Kills one character. They lose their turn.");
                    break;
                case THIEF:
                    System.out.println("Steals gold from one character.");
                    break;
                case MAGICIAN:
                    System.out.println("Swap hands or redraw cards.");
                    break;
                case KING:
                    System.out.println("Gets gold for yellow districts and crown.");
                    break;
                case BISHOP:
                    System.out.println("Gets gold for blue districts. Immune to Warlord.");
                    break;
                case MERCHANT:
                    System.out.println("Gets gold for green districts. +1 gold.");
                    break;
                case ARCHITECT:
                    System.out.println("Draw 2 extra cards. Build up to 3.");
                    break;
                case WARLORD:
                    System.out.println("Can destroy districts by paying cost-1.");
                    break;
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("No card or character matches '" + target + "'.");
        }
    }

    private int getTargetFromTokens(Game game, String[] tokens, int min, int max) {
        if (tokens.length > 1) {
            try {
                int choice = Integer.parseInt(tokens[1]);
                if (choice >= min && choice <= max) return choice;
                else System.out.println("Invalid character number. Must be between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
            return -1;
        }

        System.out.println("Enter character number (" + min + "-" + max + "): ");
        try {
            return game.getConsole().nextInt();
        } catch (Exception e) {
            System.out.println("Invalid input.");
            return -1;
        }
    }

    private int pickRandomTarget(int min, int max) {
        return new java.util.Random().nextInt(max - min + 1) + min;
    }

    public void resetBuildCount() {
        buildsThisTurn = 0;
    }

    public int getBuildCount() {
        return buildCount;
    }

    public void incrementBuildCount() {
        buildCount++;
    }

}