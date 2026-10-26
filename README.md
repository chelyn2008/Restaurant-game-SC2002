# Restaurant Rush (SC2002 Group Assignment)

## Repository

https://github.com/chelyn2008/Restaurant-game-SC2002

## Stage comparisons

Each stage is a runnable commit marked with a git tag. The source code at each tag contains only
that stage's features.

| Stage | Tag | Commit | Adds | Runnable on its own |
|-------|-----|--------|------|---------------------|
| 1 | `stage-1` | `fbd6130` | Regular customers, menu, Waiter, Chef, Cashier, full turn sequence, Victory and Defeat | Yes; no VIP, Critic, Host, combo or Happy Hour classes |
| 2 | `stage-2` | `cbc34b3` | VIP and Critic customers, configurable arrival schedule | Yes; no Host, combo or Happy Hour classes |
| 3 | `stage-3` | final commit | Host, ComboMeal, Happy Hour (with PricingPolicy), `--stage` option | Yes |

Compare links:

- Stage 1 → 2: https://github.com/chelyn2008/Restaurant-game-SC2002/compare/stage-1...stage-2
- Stage 2 → 3: https://github.com/chelyn2008/Restaurant-game-SC2002/compare/stage-2...stage-3

Commits in each stage:

| Stage | Commits |
|-------|---------|
| 1 | `869f81c` domain entities · `747b878` staff and Restaurant · `0b73a17` GameEngine, config, commands, victory check · `528f033` ConsoleUI and Main · `49989af` Stage 1 checks · `c585294` victory/defeat runs · `fbd6130` documentation |
| 2 | `20cf485` VIPCustomer and CriticCustomer · `b853b05` configurable arrival schedule · `07dbf70` Stage 2 runs · `6cced46` Stage 2 checks · `cbc34b3` documentation |
| 3 | `059ae75` ComboMeal and combo menu · `143ba9d` Host, Happy Hour, PricingPolicy · `ec4b9e6` wiring and `--stage` option · `26f2931` Stage 3 runs · `3367855` Stage 3 checks · `f3bc81f` documentation · `ba78b46` menu package and display tidy-up |

## How to run each stage individually

Requirements: Java 17 or newer (JDK), no external libraries, no network. Run every command from the
project root.

### 1. Switch to the stage

```sh
git checkout stage-1        # or stage-2, or stage-3 (commit or stash your own changes first)
```

Afterwards, return to the latest code with `git checkout dev` (or your branch name). To keep your
normal folder unchanged, open a stage in a separate folder instead:
`git worktree add ../restaurant-stage-1 stage-1`, then `cd ../restaurant-stage-1`.

### 2. Build

| Terminal | Command |
|----------|---------|
| PowerShell (VS Code on Windows) | `javac --release 17 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName` |
| Git Bash / macOS / Linux | `javac --release 17 -d out $(find src -name "*.java")` |

### 3. Play, replay the victory and defeat runs, run the checks

To replay a run in PowerShell, pipe the script in: `Get-Content runs\<script>.txt | java -cp out restaurant.Main --echo`.
In Git Bash, redirect it instead: `java -cp out restaurant.Main --echo < runs/<script>.txt`.

To build the checks in PowerShell: `javac --release 17 -d out-test (Get-ChildItem -Recurse src,test -Filter *.java).FullName`.
In Git Bash: `javac --release 17 -d out-test $(find src test -name "*.java")`.

**At `stage-1`**

| What | PowerShell command | Expected result |
|------|--------------------|-----------------|
| Play | `java -cp out restaurant.Main` | Stage 1 game, six Regular customers |
| Victory run | `Get-Content runs\victory.txt \| java -cp out restaurant.Main --echo` | VICTORY: 5 paid, $72.00, average 82.4 |
| Defeat run | `Get-Content runs\defeat.txt \| java -cp out restaurant.Main --echo` | DEFEAT: 1 paid, $15.00, average 28.0 |
| Checks | `java -cp out-test restaurant.StageOneChecks` | 52 checks passed |

**At `stage-2`**

| What | PowerShell command | Expected result |
|------|--------------------|-----------------|
| Play | `java -cp out restaurant.Main` | Stage 2 game, Critic on turn 4, VIP on turn 7 |
| Victory run | `Get-Content runs\stage2-critic-first.txt \| java -cp out restaurant.Main --echo` | VICTORY: 4 paid, $55.50, average 60.8 |
| Defeat run | `Get-Content runs\stage2-regular-first.txt \| java -cp out restaurant.Main --echo` | DEFEAT: 4 paid, $55.50, average 59.8 |
| Checks | `java -cp out-test restaurant.StageOneChecks` and `java -cp out-test restaurant.StageTwoChecks` | 52 and 38 checks passed |

At this tag the Stage 1 scripts replay with `--schedule regular`, e.g.
`Get-Content runs\victory.txt | java -cp out restaurant.Main --schedule regular --echo`.

**At `stage-3`**

| What | PowerShell command | Expected result |
|------|--------------------|-----------------|
| Play | `java -cp out restaurant.Main` | Stage 3 game, Host, combos and Happy Hour |
| Victory run | `Get-Content runs\stage3-victory.txt \| java -cp out restaurant.Main --echo` | VICTORY: 4 paid, $86.40, average 64.5 (VIP Burger $10.80) |
| Defeat run | `Get-Content runs\stage3-defeat.txt \| java -cp out restaurant.Main --echo` | DEFEAT: 4 paid, $89.10, average 53.8 |
| Checks | `java -cp out-test restaurant.StageOneChecks`, `StageTwoChecks`, `StageThreeChecks` | 52, 38 and 48 checks passed |

At this tag the earlier stages can also be played from the same code with `--stage 1` or `--stage 2`,
e.g. `java -cp out restaurant.Main --stage 1`, and their runs replay the same way:
`Get-Content runs\victory.txt | java -cp out restaurant.Main --stage 1 --echo`.

Seed and input details for every run are in [TESTING.md](TESTING.md).

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
