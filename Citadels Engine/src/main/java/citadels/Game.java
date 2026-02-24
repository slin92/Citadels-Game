package citadels;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Game{
    private List<Player> players;
    private TurnManager turnManager;
    private CardsAndDecks.DistrictDeck districtDeck;
    private CardsAndDecks.CharacterDeck characterDeck;
    private Scanner console;
    private int crownIndex;
    private int numPlayers;
    private Player currentPlayer;
    private Player firstFinisher = null;
    private boolean debugMode = false;

    
    public static void main(String[] args) {
        Game game = new Game();
        game.setup();
        game.play();
    }

    public Game(){
        districtDeck = new CardsAndDecks.DistrictDeck();
        characterDeck = new CardsAndDecks.CharacterDeck();
        players = new ArrayList<>();
        console = new Scanner(System.in);
        turnManager = new TurnManager(this);
    }

    public void setup(){
        while(true){
            System.out.print("Enter how many players [4-7]: ");
            String input = console.nextLine().trim();
            try {
                this.numPlayers = Integer.parseInt(input);
                if(numPlayers >= 4 && numPlayers <=7) break;
            } catch (NumberFormatException e) {
                //ignore
            }

            System.out.print("Enter how many players [4-7]: ");
        }

        System.out.println("Shuffling deck...");
        districtDeck.shuffle();

        System.out.println("Adding characters...");
        characterDeck = new CardsAndDecks.CharacterDeck();

        System.out.println("Dealing cards...");
        players.clear();

        players.add(new HumanPlayer("Player 1"));
        for (int i = 2; i <= numPlayers; i++) {
            players.add(new AIPlayer("CPU " + i));
        }

        for (Player p : players) {
            p.drawDistricts(districtDeck, 4);
        }

        System.out.println("Starting Citadels with " + numPlayers + " players...");
        System.out.println("You are player 1");

        crownIndex = new Random().nextInt(players.size());
    }

    public TurnManager getTurnManager() {
        return turnManager;
    }

    public void play() {
        while (!isGameOver()) {
            turnManager.runSelectionPhase();
            turnManager.runTurnPhase();  // AI and Human both handle input here
        }
        finalizeScores();
    }

    public void handleCommand(String input){
        if (currentPlayer == null) {
            System.out.println("No active player turn. Cannot process commands.");
            return;
        }

        String[] tokens = input.trim().split("\\s+");
        if (tokens.length == 0) return; 

        String cmd = tokens[0].toLowerCase();

        switch(cmd){
            case "t":
                turnManager.runSelectionPhase();
                turnManager.runTurnPhase();
                break;
            case "hand":
                currentPlayer.showHand();
                break;
            case "gold":
                currentPlayer.showGold();
                break;
            case "cards":
                System.out.println("Use 'cards' only when prompted at the start of your turn.");
                break;
            case "build":
                if (tokens.length > 1) {
                    try {
                        int index = Integer.parseInt(tokens[1]);
                        boolean built = currentPlayer.buildDistrict(index, this);
                        if (!built) {
                            System.out.println("You cannot build that.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid index format");
                    }
                } else {
                    System.out.println("Usage: build <index>");
                }
                break;
            case "city":
                if(tokens.length == 1){
                    currentPlayer.showCity();
                } else {
                    try {
                        int playerIndex = Integer.parseInt(tokens[1]) -1;
                        players.get(playerIndex).showCity();
                    } catch (Exception e) {
                        System.out.println("Invalid player number.");
                    }
                }
                break;
            case "info":
                if(tokens.length > 1){
                    String target = tokens[1];
                    currentPlayer.showInfo(target);
                } else {
                    System.out.println("Usage: info <card_index or character_name>");
                }
                break;
            case "action":
                if (currentPlayer.getCharacter() == null) {
                    System.out.println("No character assigned.");
                    break;
                }

                CardsAndDecks.Ability ability = currentPlayer.getCharacter().getAbility();

                if (tokens.length == 1) {
                    // Only "action" typed -> show help text
                    switch (ability) {
                        case MAGICIAN:
                            System.out.println("You may do:");
                            System.out.println(" - action swap <player number> (to swap hands)");
                            System.out.println(" - action redraw <id1,id2,...> (to discard and redraw cards)");
                            break;
                        case ASSASSIN:
                            System.out.println("Type action <target number> to assassinate a character.");
                            break;
                        case THIEF:
                            System.out.println("Type action <target number> to steal gold from a character.");
                            break;
                        case KING:
                            System.out.println("You receive gold for yellow districts and take the crown.");
                            break;
                        case BISHOP:
                            System.out.println("You receive gold for blue districts and are immune to the Warlord.");
                            break;
                        case MERCHANT:
                            System.out.println("You receive gold for green districts and 1 bonus gold.");
                            break;
                        case ARCHITECT:
                            System.out.println("You draw 2 extra cards and may build up to 3 districts.");
                            break;
                        case WARLORD:
                            System.out.println("You may destroy another players district by paying its cost minus 1.");
                            break;
                    }
                }

                // Always delegate to player's ability handler
                currentPlayer.performSpecialAction(this, tokens);
                break;     
            case "end":
                currentPlayer.endTurn();
                break;
            case "all":
                showAllPlayerStats();
                break;
            case "save":
                if(tokens.length > 1 ){
                    saveGame(tokens[1]);
                    System.out.println("Game saved to " + tokens[1]);
                } else {
                    System.out.println("Usage: save <filename>");
                }
                break;
            case "load":
                if(tokens.length > 1){
                    loadGame(tokens[1]);
                    System.out.println("Game loaded from " + tokens[1]);
                } else {
                    System.out.println("Usage: load <filename>");
                }
                break;
            case "help":
                showHelp();
                break;
            case "debug":
                toggleDebugMode();
                break;
            default: 
                System.out.println("Unknown command. Type 'help' for a list of commands.");
                break;
        }
    }

    /**
     * Check for game end: any player with 8+ districts in city.
     */
    public boolean isGameOver() {
        return players.stream().anyMatch(p -> p.getCity().size() >= 8);
    }

    /**
     * Calculate and display final scores and winner.
     */
    private void finalizeScores() {
        int highest = -1;
        Player winner = null;

        System.out.println("\n==========================");
        System.out.println("        GAME OVER");
        System.out.println("==========================");

        System.out.println("\n--- FINAL SCORES ---");
        for (Player p : players) {
            System.out.println("\n--- Scoring for " + p.getName() + " ---");
            int score = 0;
            boolean[] colors = new boolean[5];

            for (CardsAndDecks.DistrictCard d : p.getCity()) {
                score += d.getCost();

                if (d.getColour() != CardsAndDecks.DistrictColor.PURPLE) {
                    colors[d.getColour().ordinal()] = true;
                } else if (d.getName().equalsIgnoreCase("Haunted Quarter")) {
                    for (int i = 0; i < colors.length; i++) {
                        if (!colors[i]) {
                            colors[i] = true;
                            System.out.println(p.getName() + " uses Haunted Quarter to fill missing color: " + CardsAndDecks.DistrictColor.values()[i]);
                            break;
                        }
                    }
                }

                if (d.getName().equalsIgnoreCase("Dragon Gate")) {
                    score += 2;
                    System.out.println(p.getName() + " has Dragon Gate (+2 points)");
                }
            }

            boolean allColors = true;
            for (boolean b : colors) allColors &= b;
            if (allColors) {
                score += 3;
                System.out.println(p.getName() + " has all 5 district types (+3 points)");
            }

            if (p.getCity().size() >= 8) {
                if (p == firstFinisher) {
                    score += 4;
                    System.out.println(p.getName() + " finished first (+4 points)");
                } else {
                    score += 2;
                    System.out.println(p.getName() + " completed city (+2 points)");
                }
            }

            System.out.println(p.getName() + " final score: " + score);
            if (score > highest) {
                highest = score;
                winner = p;
            } else if (score == highest && p.getCharacter() != null && winner.getCharacter() != null) {
                if (p.getCharacter().getRank() < winner.getCharacter().getRank()) {
                    winner = p;
                }
            }
        }

        if (winner != null)
            System.out.println("\n Winner: " + winner.getName() + " with " + highest + " points!");
    }



    // Accessors for TurnManager and other classes
    public List<Player> getPlayers() { return players; }
    public CardsAndDecks.CharacterDeck getCharacterDeck() { return characterDeck; }
    public CardsAndDecks.DistrictDeck getDistrictDeck() { return districtDeck; }
    public int getCrownIndex() { return crownIndex; }
    public Scanner getConsole() { return console; }

    public void saveGame(String filename) {
        JSONObject root = new JSONObject();

        root.put("crownIndex", crownIndex);

        JSONArray playerArray = new JSONArray();
        for (Player p : players) {
            JSONObject pj = new JSONObject();
            pj.put("name", p.getName());
            pj.put("gold", p.getGold());

            pj.put("character", p.getCharacter() != null ? p.getCharacter().getAbility().name() : "");

            JSONArray handArr = new JSONArray();
            for (CardsAndDecks.DistrictCard card : p.getHand()) {
                handArr.add(card.getName());
            }
            pj.put("hand", handArr);

            JSONArray cityArr = new JSONArray();
            for (CardsAndDecks.DistrictCard card : p.getCity()) {
                cityArr.add(card.getName());
            }
            pj.put("city", cityArr);

            playerArray.add(pj);
        }

        root.put("players", playerArray);

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(root.toJSONString());
            System.out.println("Game saved to " + filename);
        } catch (IOException e) {
            System.out.println("Failed to save: " + e.getMessage());
        }
    }

    public void loadGame(String filename) {
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(filename)) {
            JSONObject root = (JSONObject) parser.parse(reader);

            // Reset all core game state
            players.clear();
            crownIndex = ((Long) root.get("crownIndex")).intValue();
            districtDeck = new CardsAndDecks.DistrictDeck();
            characterDeck = new CardsAndDecks.CharacterDeck();
            currentPlayer = null;
            turnManager = new TurnManager(this);

            JSONArray playerArray = (JSONArray) root.get("players");

            for (Object o : playerArray) {
                JSONObject pj = (JSONObject) o;

                String name = (String) pj.get("name");
                int gold = ((Long) pj.get("gold")).intValue();
                String abilityStr = (String) pj.get("character");

                Player p = name.equals("Player 1") ? new HumanPlayer(name) : new AIPlayer(name);
                p.addGold(gold - 2); // adjust for initial 2-gold default

                // Reassign character (if any)
                if (!abilityStr.isEmpty()) {
                    CardsAndDecks.Ability a = CardsAndDecks.Ability.valueOf(abilityStr);
                    CardsAndDecks.CharacterCard cc = new CardsAndDecks.CharacterCard(a.name(), a.ordinal() + 1, a);
                    p.setCharacter(cc);
                }

                // Load hand
                JSONArray hand = (JSONArray) pj.get("hand");
                p.getHand().clear(); // just in case
                for (Object c : hand) {
                    p.getHand().add(districtDeck.findByName((String) c));
                }

                // Load city
                JSONArray city = (JSONArray) pj.get("city");
                p.getCity().clear(); // just in case
                for (Object c : city) {
                    p.getCity().add(districtDeck.findByName((String) c));
                }

                players.add(p);
            }

            System.out.println("Game loaded from " + filename);
        } catch (Exception e) {
            System.out.println("Failed to load: " + e.getMessage());
        }
    }


    public void swapHands(Player a, Player b) {
        List<CardsAndDecks.DistrictCard> temp = new ArrayList<>(a.getHand());
        a.getHand().clear();
        a.getHand().addAll(b.getHand());
        b.getHand().clear();
        b.getHand().addAll(temp);
    }

    public void redrawCards(Player player, List<Integer> indexes) {
        List<CardsAndDecks.DistrictCard> toRedraw = new ArrayList<>();
        indexes.sort(Collections.reverseOrder());  // Remove higher indices first
        for (int i : indexes) {
            if (i >= 0 && i < player.getHand().size()) {
                toRedraw.add(player.getHand().remove(i));
            }
        }
        player.drawDistricts(districtDeck, toRedraw.size());
    }

    public void redrawCards(Player player, String input) {
        List<Integer> indexes = new ArrayList<>();
        for (String s : input.split(",")) {
            try {
                indexes.add(Integer.parseInt(s.trim()));
            } catch (NumberFormatException e) {
                System.out.println("Invalid index: " + s);
            }
        }
        redrawCards(player, indexes);  // calls your existing method
    }       


    public void handleWarlordAction(Player warlord) {
        List<Player> targets = new ArrayList<>();
        for (Player p : players) {
            if (!p.equals(warlord) && !p.getCity().isEmpty()) {
                targets.add(p);
            }
        }

        System.out.println("Choose a player to destroy a district from:");
        for (int i = 0; i < targets.size(); i++) {
            System.out.println(i + ": " + targets.get(i).getName());
        }

        int targetIndex = console.nextInt();
        if (targetIndex < 0 || targetIndex >= targets.size()) return;

        Player target = targets.get(targetIndex);
        List<CardsAndDecks.DistrictCard> city = target.getCity();

        System.out.println("Choose a district to destroy:");
        for (int i = 0; i < city.size(); i++) {
            CardsAndDecks.DistrictCard d = city.get(i);
            System.out.println(i + ": " + d.getName() + " (Cost " + d.getCost() + ")");
        }

        int districtIndex = console.nextInt();
        if (districtIndex < 0 || districtIndex >= city.size()) return;

        CardsAndDecks.DistrictCard toDestroy = city.get(districtIndex);
        int cost = toDestroy.getCost();

        if (warlord.getGold() >= cost - 1) {
            warlord.addGold(-(cost - 1));
            city.remove(districtIndex);
            System.out.println("Destroyed " + toDestroy.getName() + " for " + (cost - 1) + " gold.");
        } else {
            System.out.println("Not enough gold to destroy that district.");
        }
    }


    public void setCrownPlayer(Player p) {
        crownIndex = players.indexOf(p);
    }

    public void setCrownIndex(int index) {
        this.crownIndex = index;
    }

    public void showAllPlayerStats() {
        for (Player p : players) {
            System.out.println(p.getName() + ": gold=" + p.getGold() + " cards=" + p.getHand().size());
        }
    }

    public void showHelp() {
        System.out.println("Valid commands are:");
        System.out.println("- hand");
        System.out.println("- gold");
        System.out.println("- cards");
        System.out.println("- build <index>");
        System.out.println("- action");
        System.out.println("- city");
        System.out.println("- all");
        System.out.println("- end");
        System.out.println("- t");
        System.out.println("- save <filename>");
        System.out.println("- load <filename>");
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void toggleDebugMode() {
        debugMode = !debugMode;
        System.out.println("Debug mode " + (debugMode ? "enabled." : "disabled."));
    }

    public void setCurrentPlayer(Player p) {
        this.currentPlayer = p;
    }

    public Player getFirstFinisher() {
        return firstFinisher;
    }

    public void setFirstFinisher(Player p) {
        this.firstFinisher = p;
    }

    public void setConsole(Scanner scanner) {
        this.console = scanner;
    }
    
    public void resetCharacterDeck() {
        this.characterDeck = new CardsAndDecks.CharacterDeck();
    }

    public void setDebugMode(boolean value) {
        this.debugMode = value;
    }

}