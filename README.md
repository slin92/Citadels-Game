# Citadels – OOP Game Engine (Java)

A console-based Citadels-inspired game engine implemented in Java.  
This project focuses on object-oriented design, game-state management, command handling, and automated testing.

> Portfolio note: This repository is shared to demonstrate my Java/OOP skills. Do not copy for academic submission.

---

## Features

- Turn-based gameplay loop
- Human and AI players
- Game-state orchestration (round/turn flow)
- Card/deck management using external data (`cards.tsv`)
- Command-driven interaction (console)
- Scoring / end-game conditions
- JUnit test suite

---

## Code Structure

Main components:

- `App` – program entry point
- `Game` – core game state + orchestration
- `TurnManager` – handles turn / round ordering
- `Player` – shared player state/behaviour
- `HumanPlayer` – command input handling
- `AIPlayer` – AI decision logic
- `CardsAndDecks` – card models + deck loading/management

Project layout:
src/main/java/citadels/ # game source
src/main/resources/citadels/ # cards.tsv data
src/test/java/citadels/ # tests


---

## How to Run

### IntelliJ (recommended)
1. Open the project as a **Gradle** project
2. Locate and run:
   - `src/main/java/citadels/App.java`

### Terminal (Gradle)

On macOS/Linux:
```bash
./gradlew test
./gradlew run

On Windows:
gradlew test
gradlew run
If run is not configured in your Gradle file, you can run App.java directly from your IDE.

Testing
Run the full test suite:
./gradlew test

Tests live under:
src/test/java/citadels/

What this demonstrates
Object-oriented design (encapsulation, inheritance, polymorphism)
State-driven command processing (console apps)
Modular separation of responsibilities across classes
Unit testing with JUnit
Working with external data files (.tsv) for game content
