# Restaurant Rush (SC2002 Group Assignment)

## Repository

https://github.com/chelyn2008/Restaurant-game-SC2002

## Stage comparisons

Each stage is a runnable commit marked with a git tag. The source code at each tag contains only
that stage's features.

| Stage | Tag | Commit | Adds | Runnable on its own |
|-------|-----|--------|------|---------------------|
| 1 | `stage-1` | `373d83b` | Regular customers, menu, Waiter, Chef, Cashier, full turn sequence, Victory and Defeat | Yes; no VIP, Critic, Host, combo or Happy Hour classes |
| 2 | `stage-2` | `30a5f0d` | VIP and Critic customers, configurable arrival schedule | Yes; no Host, combo or Happy Hour classes |
| 3 | `stage-3` | final commit | Host, ComboMeal, Happy Hour (with PricingPolicy), `--stage` option | Yes |

Compare links:

- Stage 1 → 2: https://github.com/chelyn2008/Restaurant-game-SC2002/compare/stage-1...stage-2
- Stage 2 → 3: https://github.com/chelyn2008/Restaurant-game-SC2002/compare/stage-2...stage-3

Commits in each stage:

| Stage | Commits |
|-------|---------|
| 1 | `392f663` domain entities · `6a3033d` staff and Restaurant · `993ef5b` GameEngine, config, commands, victory check · `16d72ee` ConsoleUI and Main · `0146318` Stage 1 checks · `5b20f31` victory/defeat runs · `373d83b` documentation |
| 2 | `04591be` VIPCustomer and CriticCustomer · `25655a7` configurable arrival schedule · `1dc4866` Stage 2 runs · `df15873` Stage 2 checks · `30a5f0d` documentation |
| 3 | `b7d79a4` ComboMeal and combo menu · `9a2471f` Host, Happy Hour, PricingPolicy · `df9663b` wiring and `--stage` option · `b4606cf` Stage 3 runs · `c13fbed` Stage 3 checks · `837776f` documentation · `dfbf4a5` menu package and display tidy-up · `0e29b85` README update · `354402e` restore the missing `domain/menu` files · then the README update this tag points to |

## How to run the game

You need Java 17 or newer. Open a terminal (e.g. PowerShell in VS Code) **in the project folder**
and type the commands below.

### Step 1: Build (once)

```powershell
javac --release 17 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
```

No output means it built successfully.

### Step 2: Play a stage

| Stage | Type this |
|-------|-----------|
| Stage 1 | `java -cp out restaurant.Main --stage 1` |
| Stage 2 | `java -cp out restaurant.Main --stage 2` |
| Stage 3 | `java -cp out restaurant.Main --stage 3` |

Each turn, type one command for each staff member when asked, then press Enter
(e.g. `seat C1`, `order C1`, `serve C1`, `cook C1` or `wait`). The game shows which
commands are allowed every turn.

### Optional: replay the saved victory and defeat runs

| Stage | Victory | Defeat |
|-------|---------|--------|
| 1 | `Get-Content runs\victory.txt \| java -cp out restaurant.Main --stage 1 --echo` | `Get-Content runs\defeat.txt \| java -cp out restaurant.Main --stage 1 --echo` |
| 2 | `Get-Content runs\stage2-critic-first.txt \| java -cp out restaurant.Main --stage 2 --echo` | `Get-Content runs\stage2-regular-first.txt \| java -cp out restaurant.Main --stage 2 --echo` |
| 3 | `Get-Content runs\stage3-victory.txt \| java -cp out restaurant.Main --stage 3 --echo` | `Get-Content runs\stage3-defeat.txt \| java -cp out restaurant.Main --stage 3 --echo` |

### Optional: run the automated checks

```powershell
javac --release 17 -d out-test (Get-ChildItem -Recurse src,test -Filter *.java).FullName
java -cp out-test restaurant.StageOneChecks
java -cp out-test restaurant.StageTwoChecks
java -cp out-test restaurant.StageThreeChecks
```

Using Git Bash, macOS or Linux instead of PowerShell? Build with
`javac --release 17 -d out $(find src -name "*.java")` and replay with
`java -cp out restaurant.Main --stage 1 --echo < runs/victory.txt`.

Seed and input details for every run are in [TESTING.md](TESTING.md). To see the code exactly as
it was at an earlier stage, use its git tag, e.g. `git checkout stage-1` (then build and run as above).

## Stage 1 → 2

Full details: [CHANGES-stage1-to-2.md](CHANGES-stage1-to-2.md). Compare:
https://github.com/chelyn2008/Restaurant-game-SC2002/compare/stage-1...stage-2

**New files**

| File | Purpose |
|------|---------|
| `src/restaurant/domain/VIPCustomer.java` | Loses 5 satisfaction per waiting turn; 10% off the bill |
| `src/restaurant/domain/CriticCustomer.java` | Loses 12 per waiting turn; always orders the most expensive dish (signature dish) |
| `src/restaurant/control/CustomerFactory.java` | Creates a customer type from a constructor reference, so arrivals need no type checks |
| `test/restaurant/StageTwoChecks.java` | Stage 2 automated checks |
| `runs/stage2-critic-first.txt` / `.log`, `runs/stage2-regular-first.txt` / `.log` | Reproducible Stage 2 victory and defeat runs |
| `CHANGES-stage1-to-2.md` | Change notes for this transition |

**Existing files changed**

| File | Change | Reason |
|------|--------|--------|
| `domain/Customer.java` | New overridable hook `chooseDish(menu, strategy)` | The Critic needs its own order preference, dispatched through the customer object instead of a type check |
| `domain/Waiter.java` | Asks the customer to choose the dish; shows the VIP bill | Uses the new hook; shows the discount to the player |
| `control/ArrivalSchedule.java` | Now a list of (turn, `CustomerFactory`); `allRegular()` and `stageTwoMix()` | It used to hard-code `RegularCustomer`; the one place that names the customer classes |
| `control/GameConfig.java` | Holds an `ArrivalSchedule` instead of a list of turns | So the schedule can be selected; `baseSetting(seed)` is still the Stage 1 setting |
| `control/GameEngine.java` | One line: takes the schedule from the config | Follows from the `GameConfig` change; the turn loop is unchanged |
| `Main.java` | New `--schedule mixed\|regular` option | Lets the Stage 1 behaviour be re-checked |
| `ui/ConsoleUI.java` | Banner shows the stage and the arrivals | Display only |
| `test/restaurant/StageOneChecks.java` | Saved-log comparison starts at turn 1 | The banner now names the stage; the game from turn 1 must stay identical |
| `README.md`, `DESIGN-NOTES.md`, `TESTING.md` | Updated for Stage 2 | Documentation |

**Deleted files:** none.

## Stage 2 → 3

Full details: [CHANGES-stage2-to-3.md](CHANGES-stage2-to-3.md). Compare:
https://github.com/chelyn2008/Restaurant-game-SC2002/compare/stage-2...stage-3

**New files**

| File | Purpose |
|------|---------|
| `src/restaurant/domain/Host.java` | Seats at most one queued customer per turn, before the Waiter acts |
| `src/restaurant/domain/menu/ComboMeal.java` | Two menu items together: units added up, price 90% of the total |
| `src/restaurant/domain/Promotion.java` | Interface for a time-limited discount |
| `src/restaurant/domain/HappyHour.java` | Once per game, lasts two turns, 20% off, +15 recovery; keeps its own state |
| `src/restaurant/domain/PricingPolicy.java` | Computes the locked price: customer discount first, then active promotions |
| `src/restaurant/control/HostCommand.java` | The manager's Host choice: `seat <id>` or `wait` |
| `test/restaurant/StageThreeChecks.java` | Stage 3 automated checks |
| `runs/stage3-victory.txt` / `.log`, `runs/stage3-defeat.txt` / `.log` | Reproducible Stage 3 victory and defeat runs (same seed, one different choice) |
| `CHANGES-stage2-to-3.md`, `CLASS-DIAGRAM.md` | Change notes; full class list for the class diagram |

**Existing files changed**

| File | Change | Reason |
|------|--------|--------|
| `domain/Restaurant.java` | Owns the Host, Happy Hour and PricingPolicy; shared seating check; Happy Hour recovery | Composition: the Happy Hour state and its rules live in the domain, not the UI; Host and Waiter share one seating rule |
| `domain/Waiter.java` | Gets the locked price from `PricingPolicy` | Discounts now stack, so their order is decided in one place |
| `domain/Customer.java` | New `recoverSatisfaction(points)`, capped at 100 | Happy Hour recovery while keeping satisfaction private and in range |
| `domain/Menu.java`, `domain/MenuItem.java` | Moved to `domain/menu/` (package `restaurant.domain.menu`); `Menu.withCombos()` added | Groups the menu classes; they need no package-private access, so no visibility changed |
| Classes that use the menu (18 files) | Import `restaurant.domain.menu` | Follows from the package move |
| `control/GameConfig.java` | Stage number, menu, Host and Happy Hour switches; `stageThree()` and `forStage(n)` | Features are switched on per stage, so Stages 1 and 2 still play unchanged |
| `control/GameEngine.java` | Happy Hour offer and recovery at turn start; Host before Waiter | The two new turn steps and the Host |
| `ui/GameUI.java`, `ui/ConsoleUI.java` | Ask for Happy Hour and the Host's task; show Happy Hour status; restyled banner | The engine needs two new decisions; display only |
| `Main.java` | `--stage 1\|2\|3` replaces `--schedule` | Selects a whole stage, not just the arrivals |
| `test/restaurant/ScriptedUI.java`, `GreedyUI.java` | Implement the new `GameUI` methods | Test UIs must match the interface |
| `runs/stage2-*.log` | Re-recorded | Only the banner changed; every turn and result is identical to the Stage 2 logs |
| `README.md`, `DESIGN-NOTES.md`, `TESTING.md` | Updated for Stage 3 | Documentation |

**Deleted files:** none (`Menu.java` and `MenuItem.java` were moved, not deleted).

## Video timestamps

| # | Segment | Time |
|---|---------|------|
| 1 | The game launching from the submitted project | |
| 2 | Meaningful manager choices, including a decision where staff are constrained | |
| 3 | A dish going READY → SERVED → payment | |
| 4 | A VIP or Critic behaviour | |
| 5 | A Stage 3 feature | |
| 6 | One victory and one defeat | |
| 7 | The stage tag comparisons and the diagrams | |
