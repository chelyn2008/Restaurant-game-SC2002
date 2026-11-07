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
| 3 | `stage-3` | `7747f26` | Host, ComboMeal, Happy Hour (with PricingPolicy), `--stage` option, extra feature: leave warnings and hints (`--hints`) | Yes |

Compare links:

- Stage 1 → 2: https://github.com/chelyn2008/Restaurant-game-SC2002/compare/stage-1...stage-2
- Stage 2 → 3: https://github.com/chelyn2008/Restaurant-game-SC2002/compare/stage-2...stage-3

Commits in each stage:

| Stage | Commits |
|-------|---------|
| 1 | `392f663` domain entities · `6a3033d` staff and Restaurant · `993ef5b` GameEngine, config, commands, victory check · `16d72ee` ConsoleUI and Main · `0146318` Stage 1 checks · `5b20f31` victory/defeat runs · `373d83b` documentation |
| 2 | `04591be` VIPCustomer and CriticCustomer · `25655a7` configurable arrival schedule · `1dc4866` Stage 2 runs · `df15873` Stage 2 checks · `30a5f0d` documentation |
| 3 | `b7d79a4` ComboMeal and combo menu · `9a2471f` Host, Happy Hour, PricingPolicy · `df9663b` wiring and `--stage` option · `b4606cf` Stage 3 runs · `c13fbed` Stage 3 checks · `837776f` documentation · `dfbf4a5` menu package and display tidy-up · `0e29b85` README update · `354402e` restore the missing `domain/menu` files · `5251795` extra feature (leave warnings and hints) · `3ceb226` merge with the class diagrams · `1e0e6e1`, `53b0c40`, `5994220` README and comment updates · `2a58eac` clearer customer-id error, staff-named hints, end-of-game warning · `ef2e465`, `f552daf` class diagrams updated for the extra feature · `f9a6f01` console output restored to match the saved runs · `7747f26` sequence diagram (this tag) |

## Class diagrams

One UML class diagram per stage, drawn in draw.io. Each one matches the code at its tag. All three
use the same layout, so a class added in Stage 2 or 3 fills a space that was empty before
(marked `NEW in Stage N`; added members are **bold** with `*`).

| Stage | Tag | Diagram | draw.io source |
|-------|-----|---------|----------------|
| 1 | `stage-1` | [PNG](ClassDiagram/class_stage1.png) · [SVG](ClassDiagram/class_stage1.svg) | [class_stage1.drawio](ClassDiagram/class_stage1.drawio) |
| 2 | `stage-2` | [PNG](ClassDiagram/class_stage2.png) · [SVG](ClassDiagram/class_stage2.svg) | [class_stage2.drawio](ClassDiagram/class_stage2.drawio) |
| 3 (final) | `stage-3` | [PNG](ClassDiagram/class_stage3.png) · [SVG](ClassDiagram/class_stage3.svg) | [class_stage3.drawio](ClassDiagram/class_stage3.drawio) |

A text list of every class, attribute and operation is in [CLASS-DIAGRAM.md](CLASS-DIAGRAM.md).

## Sequence diagram

One complete Stage 3 scenario, drawn in draw.io, matching the code at tag `stage-3`. A VIP customer
(C3) arrives, the manager switches on Happy Hour, and the order is priced with the VIP discount
first and then Happy Hour. The scenario follows the dish from READY to SERVED, then eating, payment
and the Victory or Defeat check after turn 18. It also shows the `--hints` extra feature. Guards and
`alt` / `loop` / `opt` frames show the manager's decisions and the invalid-choice retries.

| Pages | Diagram | draw.io source |
|-------|---------|----------------|
| 1. Turn start (arrival, Happy Hour, recovery) · 2. Leave warnings · 3. Host seats C3, `hint` · 4. Waiter: order and serve · 5. Chef, eating, payment, waiting · 6. Result check | [PDF](SequenceDiagram/Sequence_Diagram_Stage3_with_hints.pdf) | [Sequence_Diagram_Stage3_with_hints.drawio](SequenceDiagram/Sequence_Diagram_Stage3_with_hints.drawio) |

## How to run the game

You need Java 17 or newer. Open a terminal (e.g. PowerShell in VS Code) **in the project folder**
and type the commands below.

### Step 1: Build (once)

```powershell
javac --release 17 -d out (Get-ChildItem -Recurse src -Filter "*.java").FullName
```

No output means it built successfully.

### Step 2: Play a stage

| Stage | Switch branch |
|-------|---------------|
| Stage 1 | `git checkout stage-1` | 
| Stage 2 | `git checkout stage-2` |
| Stage 3 | `git checkout stage-3` |

To run the game after switching the branch 
```powershell
java -cp out restaurant.Main
```

For stage 3 with hints, run this after switching to the 3rd stage
```powershell
java -cp out restaurant.Main --stage 3 --hints
```

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

## Extra feature: leave warnings and hints (`--hints`)

An optional helper for the manager, added in Stage 3 and **off by default**. Turn it on with
`java -cp out restaurant.Main --stage 3 --hints`.

- **Leave warnings:** at the start of every turn, each waiting customer shows when they will leave
  if not served, e.g. `Warning: C1 (Critic, satisfaction 100, -12 per turn) leaves at the end of turn 9 unless served.`
  If that turn would be after turn 18, the warning says the customer
  `will not leave before the game ends (turn 18)` instead.
- **`hint` command:** type `hint` at any Host, Waiter or Chef prompt to see the most urgent customer
  and their next step, e.g. `Hint: most urgent is C2 (Critic) ... Next step: Host or Waiter: seat C2.`
  The next step names who can do it: `Host or Waiter: seat`, `Waiter: order`, `Chef: cook` or
  `Waiter: serve` (without a Host, as in Stages 1-2, seating names only the Waiter). A hint does not
  use up the staff member's task.

**How the leaving turn is calculated:** a customer waiting for food loses satisfaction at the end of
every turn (Regular 8, VIP 5, Critic 12) and leaves at 0. So

- turns left = satisfaction ÷ loss per turn, rounded up (the current turn counts as the first)
- leaving turn = current turn + turns left − 1

Example: on turn 4 a Critic at 64 has 64 ÷ 12 = 5.33, so 6 turns left, and leaves at the end of
turn 4 + 6 − 1 = 9. A Regular at 60 on turn 18 has 8 turns left (turn 25), which is after the game
ends, so it is shown as "will not leave before the game ends". Happy Hour recovery is not counted,
so the warning is on the safe side.

**Design:** the calculation is in a new domain class, `ServiceAdvisor`, not in the UI. It asks each
customer for `getSatisfactionLossPerTurn()`, so a Regular (8), VIP (5) and Critic (12) get different
warnings with no type checks (polymorphism). It only reads the game state, so it cannot change any
rule. Without `--hints` the game and every saved run are exactly as before; all checks still pass,
including 13 new checks for this feature in `StageThreeChecks`. More detail:
[DESIGN-NOTES.md](DESIGN-NOTES.md) and [CHANGES-stage2-to-3.md](CHANGES-stage2-to-3.md).

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
| `src/restaurant/domain/ServiceAdvisor.java` | **Extra feature** (`--hints`, off by default): when each waiting customer will leave, and the most urgent customer's next step |
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
| **Extra feature (`--hints`):** `GameConfig`, `GameEngine`, `HostCommand`, `WaiterCommand`, `ChefCommand`, `CommandParsing`, `ConsoleUI`, `Main` | `hintsEnabled` + `withHints()`; engine shows leave warnings and answers `hint` without using the task; `HINT` command action; one banner line when on; `--hints` option | Optional advice for the manager. Off by default, so every required rule, saved run and earlier check is unchanged |
| `runs/stage2-*.log` | Re-recorded | Only the banner changed; every turn and result is identical to the Stage 2 logs |
| `control/GameEngine.java` (display) | After the Host or Waiter acts, shows e.g. `Chef can now: cook C1` if that action opened a new task (through the existing `showMessage`) | The start-of-turn list could look out of date; no rule or interface changed |
| `runs/*.log` (all six) | Re-recorded again for the "can now" lines | 42 lines added, none removed or changed: same turns, choices and results |
| `README.md`, `DESIGN-NOTES.md`, `TESTING.md` | Updated for Stage 3 | Documentation |

**Deleted files:** none 

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

## AI use history

Each member's full AI conversations (prompts, replies and follow-ups, in order) are on their own
branch in this repository, under `ai-history/stage-N/chat-NN.md`.

| Member | Branch | Chats |
|--------|--------|-------|
| Jingyi | [`jingyi-ai-history`](https://github.com/chelyn2008/Restaurant-game-SC2002/tree/jingyi-ai-history/ai-history) | [Stage 3, chat 01](https://github.com/chelyn2008/Restaurant-game-SC2002/blob/jingyi-ai-history/ai-history/stage-3/chat-01.md): sequence diagram requirements and use case |
| Caiying | [`caiying-ai-history`](https://github.com/chelyn2008/Restaurant-game-SC2002/tree/caiying-ai-history/ai-history) | [Stage 3, chat 01](https://github.com/chelyn2008/Restaurant-game-SC2002/blob/caiying-ai-history/ai-history/stage-3/chat-01.md): final requirement check, code review and fixes |
| Ziqi | [`ziqi-ai-history`](https://github.com/chelyn2008/Restaurant-game-SC2002/tree/ziqi-ai-history/ai-history) | [Stage 3, chat 01](https://github.com/chelyn2008/Restaurant-game-SC2002/blob/ziqi-ai-history/ai-history/stage-3/chat-01.md): UML class diagrams (Stages 1–3), the --hints update, class diagram walkthrough and fixes |
