# Restaurant Rush (SC2002 Group Assignment)

A command-line, turn-based restaurant management game in Java. You are the manager:
each turn you choose what the Waiter and the Chef do. Customers arrive, wait, order,
eat and pay; if they wait too long they leave unhappy.

**Current stage: Stage 1 (basic restaurant operation).** Regular customers only;
Waiter, Chef and Cashier; full 18-turn sequence with Victory and Defeat.

## Requirements

- Java 17 or newer (JDK). No external libraries, no network access needed.

## Build and run

From the project root:

```sh
# Build the game
javac --release 17 -d out $(find src -name "*.java")

# Play (default seed 2002)
java -cp out restaurant.Main

# Play with another seed for the customers' food choices
java -cp out restaurant.Main --seed 7

# Replay a saved run from a script (--echo prints each command into the output)
java -cp out restaurant.Main --echo < runs/victory.txt
java -cp out restaurant.Main --echo < runs/defeat.txt

# Check a replay is identical to the saved log (no output = identical)
java -cp out restaurant.Main --echo < runs/victory.txt | diff --strip-trailing-cr - runs/victory.log
```

On Windows (PowerShell), build with:

```powershell
javac --release 17 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
Get-Content runs\victory.txt | java -cp out restaurant.Main --echo
```

## Automated checks

```sh
javac --release 17 -d out-test $(find src test -name "*.java")
java -cp out-test restaurant.StageOneChecks
```

Run from the project root (the last check replays `runs/*.txt`). It prints `PASS`/`FAIL`
per check and exits with code 1 if any check fails. See [TESTING.md](TESTING.md).

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
src/restaurant/Main.java     entry point (--seed N, --echo)
src/restaurant/domain/       entities and rules: Customer, Order, Table, Menu, Waiter, Chef, Cashier, Restaurant ...
src/restaurant/control/      GameEngine (turn loop), GameConfig, ArrivalSchedule, commands, VictoryCondition
src/restaurant/ui/           GameUI interface and ConsoleUI
test/restaurant/             StageOneChecks and test UIs (ScriptedUI, GreedyUI, FixedFoodChoice)
runs/                        victory/defeat input scripts and their console logs
```

Design notes for the report: [DESIGN-NOTES.md](DESIGN-NOTES.md). Test evidence: [TESTING.md](TESTING.md).

## Stages

| Stage | Tag | Adds |
|-------|-----|------|
| 1 | `stage-1` | Regular customers, menu, Waiter, Chef, Cashier, full turn sequence, both endings |
| 2 | `stage-2` | (to do) VIP and Critic customers |
| 3 | `stage-3` | (to do) Host, ComboMeal, Happy Hour |

Stage transition notes (new/changed files and compare links) and video timestamps will be
added here as later stages are completed.
