# Restaurant Rush (SC2002 Group Assignment)

Repository: https://github.com/chelyn2008/Restaurant-game-SC2002

A command-line, turn-based restaurant management game in Java. You are the manager:
each turn you choose what the Host, Waiter and Chef do, and when to run Happy Hour.
Customers arrive, wait, order, eat and pay; if they wait too long they leave unhappy.

**Current stage: Stage 3 (new role and menu policy).** Regular, VIP and Critic customers;
Host, Waiter, Chef and Cashier; combo meals; Happy Hour; full 18-turn sequence with Victory
and Defeat. Stages 1 and 2 are still playable with `--stage 1` / `--stage 2`.

| Customer | Loses per waiting turn | Bill | Special behaviour |
|----------|------------------------|------|-------------------|
| Regular | 8 | Full price | — |
| VIP | 5 | 10% off (nearest cent) | — |
| Critic | 12 | Full price | Signature dish: always orders the most expensive item (Pasta in Stages 1-2, the Burger + Pasta Combo in Stage 3) |

| Menu (Stage 3) | Price | Prep units |
|----------------|-------|------------|
| Salad / Burger / Pasta | $9.00 / $15.00 / $18.00 | 1 / 1 / 2 |
| Salad + Burger Combo | $21.60 | 2 |
| Salad + Pasta Combo | $24.30 | 3 |
| Burger + Pasta Combo | $29.70 | 3 |

**Happy Hour** (once per game, at the start of a turn, lasts that turn and the next): orders taken
while it is active get 20% off (after the VIP discount: a VIP Burger costs $10.80), and customers
still waiting for a table or food recover 15 satisfaction at the start of each active turn (max 100).
The price is locked when the order is taken.

## Requirements

- Java 17 or newer (JDK). No external libraries, no network access needed.

## Build and run

From the project root (Git Bash / macOS / Linux):

```sh
# Build the game
javac --release 17 -d out $(find src -name "*.java")

# Play Stage 3 (default seed 2002)
java -cp out restaurant.Main

# Other options: another seed for the customers' food choices, or an earlier stage
java -cp out restaurant.Main --seed 7
java -cp out restaurant.Main --stage 1      # six Regular customers, base menu, no Host / Happy Hour
java -cp out restaurant.Main --stage 2      # adds the VIP and the Critic

# Replay saved runs (--echo prints each command into the output)
java -cp out restaurant.Main --echo < runs/stage3-victory.txt
java -cp out restaurant.Main --echo < runs/stage3-defeat.txt
java -cp out restaurant.Main --stage 2 --echo < runs/stage2-critic-first.txt
java -cp out restaurant.Main --stage 1 --echo < runs/victory.txt

# Check a replay is identical to the saved log (no output = identical)
java -cp out restaurant.Main --echo < runs/stage3-victory.txt | diff --strip-trailing-cr - runs/stage3-victory.log
```

In PowerShell (the VS Code default on Windows), `<` does not work; pipe the file instead:

```powershell
javac --release 17 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out restaurant.Main
Get-Content runs\stage3-victory.txt | java -cp out restaurant.Main --echo
Get-Content runs\victory.txt | java -cp out restaurant.Main --stage 1 --echo
```

## Automated checks

```sh
javac --release 17 -d out-test $(find src test -name "*.java")
java -cp out-test restaurant.StageOneChecks
java -cp out-test restaurant.StageTwoChecks
java -cp out-test restaurant.StageThreeChecks
```

PowerShell: `javac --release 17 -d out-test (Get-ChildItem -Recurse src,test -Filter *.java).FullName`.
Run from the project root (some checks replay `runs/*.txt` and scan `src/`). Each prints
`PASS`/`FAIL` per check and exits with code 1 if any check fails. See [TESTING.md](TESTING.md).

## How to play

Each turn shows the revenue, Happy Hour status, the waiting queue, both tables (customer,
satisfaction, order status) and the tasks each staff member can do. While Happy Hour is unused you
are first asked whether to switch it on (`yes` / `no`). Then type one command for each staff member,
in this order:

| Staff  | Commands |
|--------|----------|
| Host   | `seat <id>` (seat a queued customer at the lowest free table), `wait` |
| Waiter | `seat <id>`, `order <id>` (take a seated customer's order), `serve <id>` (serve a READY order), `wait` |
| Chef   | `cook <id>` (one preparation unit of that customer's order), `wait` |

Customer ids are `C1` to `C6` (`c1` or `1` also work). An invalid choice is explained and you choose
again; it does not use up the task. The Cashier works automatically. In `--stage 1` and `--stage 2`
there is no Host and no Happy Hour question.

**Goal after turn 18:** at least 4 paid customers, revenue at least $45.00, and average
satisfaction of paid customers at least 60.

## Project layout

```
src/restaurant/Main.java     entry point (--stage 1|2|3, --seed N, --echo)
src/restaurant/domain/       entities and rules: Customer (Regular, VIP, Critic), Order, Table, Menu, MenuItem, ComboMeal,
                             Staff (Host, Waiter, Chef, Cashier), Promotion, HappyHour, PricingPolicy, Restaurant ...
src/restaurant/control/      GameEngine (turn loop), GameConfig, ArrivalSchedule, CustomerFactory, commands, VictoryCondition
src/restaurant/ui/           GameUI interface and ConsoleUI
test/restaurant/             StageOneChecks, StageTwoChecks, StageThreeChecks and test UIs
runs/                        input scripts and console logs (Stage 1: victory/defeat; Stage 2: stage2-*; Stage 3: stage3-*)
```

Report material: [DESIGN-NOTES.md](DESIGN-NOTES.md) (design discussion),
[CLASS-DIAGRAM.md](CLASS-DIAGRAM.md) (every class with attributes, operations and relationships),
[TESTING.md](TESTING.md) (checks and reproducible runs).

## Stages

| Stage | Tag | Adds |
|-------|-----|------|
| 1 | `stage-1` | Regular customers, menu, Waiter, Chef, Cashier, full turn sequence, both endings |
| 2 | `stage-2` | VIP and Critic customers, configurable arrival schedule |
| 3 | `stage-3` | Host, ComboMeal, Happy Hour (with PricingPolicy), `--stage` option |

### Stage 1 → 2

- New files: `VIPCustomer`, `CriticCustomer`, `CustomerFactory`, `StageTwoChecks`, `runs/stage2-*`.
- Changed: `Customer` (new `chooseDish` hook), `Waiter` (uses the hook, shows the VIP bill),
  `ArrivalSchedule`, `GameConfig`, `GameEngine` (one line), `Main` (`--schedule`), `ConsoleUI`
  (banner), `StageOneChecks` (log comparison skips the banner). Nothing deleted.
- Reasons for each change: [CHANGES-stage1-to-2.md](CHANGES-stage1-to-2.md).
- Compare: https://github.com/chelyn2008/Restaurant-game-SC2002/compare/stage-1...stage-2

### Stage 2 → 3

- New files: `Host`, `ComboMeal`, `Promotion`, `HappyHour`, `PricingPolicy`, `HostCommand`,
  `StageThreeChecks`, `runs/stage3-*`, `CHANGES-stage2-to-3.md`, `CLASS-DIAGRAM.md`.
- Changed: `Restaurant` (owns Host, Happy Hour and PricingPolicy; shared seating check; recovery),
  `Waiter` (price from PricingPolicy), `Customer` (`recoverSatisfaction`), `Menu` (`withCombos`),
  `GameConfig` (per-stage features), `GameEngine` (Happy Hour and Host steps), `GameUI` / `ConsoleUI`
  (two new questions, Happy Hour display), `Main` (`--stage` replaces `--schedule`), test UIs. Nothing deleted.
- Reasons for each change: [CHANGES-stage2-to-3.md](CHANGES-stage2-to-3.md).
- Compare: https://github.com/chelyn2008/Restaurant-game-SC2002/compare/stage-2...stage-3

## Video timestamps

To be added when the video is recorded:

| # | Segment | Time |
|---|---------|------|
| 1 | The game launching from the submitted project | |
| 2 | Meaningful manager choices, including a decision where staff are constrained | |
| 3 | A dish going READY → SERVED → payment | |
| 4 | A VIP or Critic behaviour | |
| 5 | A Stage 3 feature | |
| 6 | One victory and one defeat | |
| 7 | The stage tag comparisons and the diagrams | |
