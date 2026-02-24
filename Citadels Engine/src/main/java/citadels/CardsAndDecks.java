package citadels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CardsAndDecks{
    public enum DistrictColor{
        RED,
        YELLOW,
        GREEN,
        BLUE,
        PURPLE;
    }

    public enum Ability {
        ASSASSIN,
        THIEF,
        MAGICIAN,
        KING,
        BISHOP,
        MERCHANT,
        ARCHITECT,
        WARLORD
    }


    public static class DistrictCard{
        private final String name;
        private final int cost;
        private final DistrictColor colour;
        private final String text;

        public DistrictCard(String name, int cost, DistrictColor colour, String text){
            this.name = name;
            this.cost = cost;
            this.colour = colour;
            this.text = text;
        }

        //Getters for District Card:
        public String getName(){
            return name;
        }

        public int getCost(){
            return cost;
        }

        public DistrictColor getColour(){
            return colour;
        }
        
        public String getText(){
            return text;
        }

        @Override
        public String toString() {
            return String.format("%s (%s, cost %d): %s", name, colour, cost, text);
        }
    }

    public static class DistrictDeck {
        private List<DistrictCard> cards = new ArrayList<>();
        private Random rng = new Random();

        public DistrictDeck() {
            try (InputStream in = getClass().getResourceAsStream("/citadels/cards.tsv");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;

                boolean isFirstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue; // skip header
                    }

                    String[] parts = line.split("\t");
                    if (parts.length < 4) continue;

                    String name = parts[0];
                    int quantity = Integer.parseInt(parts[1]);
                    DistrictColor color = DistrictColor.valueOf(parts[2].toUpperCase());
                    int cost = Integer.parseInt(parts[3]);
                    String text = parts.length > 4 ? parts[4] : "";

                    for (int i = 0; i < quantity; i++) {
                        cards.add(new DistrictCard(name, cost, color, text));
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to load cards.tsv: " + e.getMessage());
            }
        }

        public void shuffle() {
            Collections.shuffle(cards, rng);
        }

        public DistrictCard draw() {
            return cards.isEmpty() ? null : cards.remove(0);
        }

        public void returnCard(DistrictCard card) {
            cards.add(card);
            shuffle();
        }

        public int size() {
            return cards.size();
        }

        public DistrictCard findByName(String name) {
            for (DistrictCard c : cards) {
                if (c.getName().equals(name)) return new DistrictCard(c.getName(), c.getCost(), c.getColour(), c.getText());
            }
            return null;
        }
    }


    public static class CharacterCard{
        private final String name;
        private final int rank;
        private final Ability ability;

        public CharacterCard(String name, int rank, Ability ability){
            this.name = name;
            this.rank = rank;
            this.ability = ability;
        }

        public String getName(){
            return name;
        }

        public int getRank(){
            return rank;
        }

        public Ability getAbility(){
            return ability;
        }

        @Override
        public String toString(){
            return String.format("%s (Rank %d): %s", name, rank, ability);
        }

    }

    public static class CharacterDeck{
        private final List<CharacterCard> cards = new ArrayList<>();

        public CharacterDeck() {
            cards.add(new CharacterCard("Assassin",    1, Ability.ASSASSIN));
            cards.add(new CharacterCard("Thief",       2, Ability.THIEF));
            cards.add(new CharacterCard("Magician",    3, Ability.MAGICIAN));
            cards.add(new CharacterCard("King",        4, Ability.KING));
            cards.add(new CharacterCard("Bishop",      5, Ability.BISHOP));
            cards.add(new CharacterCard("Merchant",    6, Ability.MERCHANT));
            cards.add(new CharacterCard("Architect",   7, Ability.ARCHITECT));
            cards.add(new CharacterCard("Warlord",     8, Ability.WARLORD));

            Collections.shuffle(cards);
        }

        //draws a card after shuffling 
        public CharacterCard drawForSelection(){
            if(cards.isEmpty()){
                System.err.println("Character deck is empty - cannot draw.");
                return null;
            }
            return cards.remove(0);
        }

        //checks how many characters are left
        public int size(){
            return cards.size();
        }

        public void shuffle() {
            Collections.shuffle(cards);
        }


        public void returnCardToDeck(CharacterCard c) {
            cards.add(c);
            Collections.shuffle(cards);
        }

        public List<CardsAndDecks.CharacterCard> getAvailableCards() {
            return new ArrayList<>(cards);  // Assuming `available` is your pool
        }

        public CardsAndDecks.CharacterCard drawAt(int index) {
            return cards.remove(index);
        }

        public void removeCard(CardsAndDecks.CharacterCard card) {
            cards.remove(card);
        }

        public String getNameByRank(int rank) {
            for (CharacterCard c : cards) {
                if (c.getRank() == rank) {
                    return c.getName();
                }
            }
            return "Unknown";
        }

        public void setAvailableCards(List<CardsAndDecks.CharacterCard> newList) {
            cards.clear();               // remove old content
            cards.addAll(newList);       // add updated list
        }
    }

    public static void main(String[] args){
        
    }
}