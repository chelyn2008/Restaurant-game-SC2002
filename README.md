# Restaurant Rush (SC2002 Group Assignment)

A command-line, turn-based restaurant management game in Java. You are the manager:
each turn you choose what the Waiter and the Chef do. Customers arrive, wait, order,
eat and pay; if they wait too long they leave unhappy.

**Current stage: Stage 2 (customer behaviour).** Regular, VIP and Critic customers;
Waiter, Chef and Cashier; full 18-turn sequence with Victory and Defeat.

| Customer | Loses per waiting turn | Bill | Special behaviour |
|----------|------------------------|------|-------------------|
| Regular | 8 | Full price | — |
| VIP | 5 | 10% off (nearest cent) | — |
| Critic | 12 | Full price | Signature dish: always orders the most expensive dish (Pasta) |

## Requirements

- Java 17 or newer (JDK). No external libraries, no network access needed.

## Build and run

From the project root:

```sh
# Build the game
javac --release 17 -d out $(find src -name "*.java")

# Play (default seed 2002, Stage 2 mixed arrivals: Critic on turn 4, VIP on turn 7)
java -cp out restaurant.Main

# Play with another seed for the customers' food choices
java -cp out restaurant.Main --seed 7

# Stage 1 arrivals (six Regular customers), to re-check Stage 1 behaviour
java -cp out restaurant.Main --schedule regular

# Replay a saved Stage 2 run (--echo prints each command into the output)
java -cp out restaurant.Main --echo < runs/stage2-critic-first.txt
java -cp out restaurant.Main --echo < runs/stage2-regular-first.txt

# Replay the Stage 1 runs
java -cp out restaurant.Main --schedule regular --echo < runs/victory.txt
java -cp out restaurant.Main --schedule regular --echo < runs/defeat.txt

# Check a replay is identical to the saved log (no output = identical)
java -cp out restaurant.Main --echo < runs/stage2-critic-first.txt | diff --strip-trailing-cr - runs/stage2-critic-first.log
```

On Windows (PowerShell), build with:

```powershell
javac --release 17 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
Get-Content runs\stage2-critic-first.txt | java -cp out restaurant.Main --echo
```

## Automated checks

```sh
javac --release 17 -d out-test $(find src test -name "*.java")
java -cp out-test restaurant.StageOneChecks
java -cp out-test restaurant.StageTwoChecks
```

Run from the project root (some checks replay `runs/*.txt` and scan `src/`). Each prints
`PASS`/`FAIL` per check and exits with code 1 if any check fails. See [TESTING.md](TESTING.md).

## How to play

Each turn shows the revenue, the waiting queue, both tables (customer, satisfaction,
order status) and the tasks each staff member can do. Then type one command for the
Waiter and one for the Chef:

| Staff  | Commands |
|--------|----------|
| Waiter | `seat <id>` (seat a queued customer at the lowest free table), `order <id>` (take a seated customer's order), `serve <id>` (serve a READY order), `wait` |
| Chef   | `cook <id>` (one preparation unit of that customer's order), `wait` |

Customer ids are `C1` to `C6` (`c1` or `1` also work). An invalid choice is explained and
you choose again; it does not use up the task. The Cashier works automatically.

**Goal after turn 18:** at least 4 paid customers, revenue at least $45.00, and average
satisfaction of paid customers at least 60.

## Project layout

```
src/restaurant/Main.java     entry point (--seed N, --schedule mixed|regular, --echo)
src/restaurant/domain/       entities and rules: Customer (Regular, VIP, Critic), Order, Table, Menu, Waiter, Chef, Cashier, Restaurant ...
src/restaurant/control/      GameEngine (turn loop), GameConfig, ArrivalSchedule, CustomerFactory, commands, VictoryCondition
src/restaurant/ui/           GameUI interface and ConsoleUI
test/restaurant/             StageOneChecks, StageTwoChecks and test UIs (ScriptedUI, GreedyUI, FixedFoodChoice)
runs/                        input scripts and their console logs (Stage 1: victory/defeat; Stage 2: stage2-*)
```

Design notes for the report: [DESIGN-NOTES.md](DESIGN-NOTES.md). Test evidence: [TESTING.md](TESTING.md).

## Stages

| Stage | Tag | Adds |
|-------|-----|------|
| 1 | `stage-1` | Regular customers, menu, Waiter, Chef, Cashier, full turn sequence, both endings |
| 2 | `stage-2` | VIP and Critic customers, configurable arrival schedule |
| 3 | `stage-3` | (to do) Host, ComboMeal, Happy Hour |

### Stage 1 → 2

- New files: `VIPCustomer`, `CriticCustomer`, `CustomerFactory`, `StageTwoChecks`, `runs/stage2-*`.
- Changed: `Customer` (new `chooseDish` hook), `Waiter` (uses the hook, shows the VIP bill),
  `ArrivalSchedule`, `GameConfig`, `GameEngine` (one line), `Main` (`--schedule`), `ConsoleUI`
  (banner), `StageOneChecks` (log comparison skips the banner). Nothing deleted.
- Reasons for each change: [CHANGES-stage1-to-2.md](CHANGES-stage1-to-2.md).
- Compare: https://github.com/chelyn2008/Restaurant-game-SC2002/compare/stage-1...stage-2

Video timestamps will be added here when the video is recorded.
