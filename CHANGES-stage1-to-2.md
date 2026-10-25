# Changes from Stage 1 to Stage 2

Stage 2 adds VIP and Critic customers alongside Regular customers. The turn steps and all Stage 1
rules are unchanged; the new behaviour lives in two new `Customer` subclasses.

Compare link (after both tags are pushed):
https://github.com/chelyn2008/Restaurant-game-SC2002/compare/stage-1...stage-2

## New files

| File | Purpose |
|------|---------|
| `src/restaurant/domain/VIPCustomer.java` | Loses 5 satisfaction per waiting turn; `calculateBill` gives 10% off, rounded to the nearest cent. |
| `src/restaurant/domain/CriticCustomer.java` | Loses 12 per waiting turn, pays full price; overrides `chooseDish` to always order the most expensive dish (its special behaviour, see below). |
| `src/restaurant/control/CustomerFactory.java` | One-method interface `create(id, arrivalTurn)`. The schedule stores constructor references (`RegularCustomer::new`, ...) so customers of any type are created without type checks. |
| `test/restaurant/StageTwoChecks.java` | Stage 2 automated checks (see TESTING.md). |
| `runs/stage2-critic-first.txt` / `.log` | Reproducible Stage 2 Victory run. |
| `runs/stage2-regular-first.txt` / `.log` | Reproducible Stage 2 Defeat run (one decision differs). |
| `CHANGES-stage1-to-2.md` | This file. |

## Existing files changed

| File | Change | Why |
|------|--------|-----|
| `domain/Customer.java` | Added one overridable hook, `chooseDish(menu, defaultChoice)`. The default just asks the food-choice strategy, exactly as before. | The Critic needs a different order preference. Putting the hook on `Customer` keeps the behaviour inside the customer object (polymorphic dispatch) instead of a type check in the Waiter or engine. |
| `domain/Waiter.java` | `takeOrder` calls `customer.chooseDish(...)` instead of `foodChoice.choose(...)`, and adds "VIP bill: $x" to the message when the bill differs from the menu price. | Uses the new hook; shows the discount to the player. The check compares two numbers, not types. Regular output is byte-for-byte unchanged. |
| `control/ArrivalSchedule.java` | Now a list of `(turn, CustomerFactory)` arrivals, with `allRegular()` (Stage 1) and `stageTwoMix()` (VIP and Critic replace two Regulars), `describe()` for the banner and `getArrivals()`. | It used to hard-code `new RegularCustomer`. This is the one setup/registration place that names the concrete customer classes. |
| `control/GameConfig.java` | `arrivalTurns` (list of ints) replaced by `arrivalSchedule`. New `baseSetting(seed, schedule)`; `baseSetting(seed)` still gives the Stage 1 all-Regular setting. | The schedule must be selectable. Keeping `baseSetting(seed)` means Stage 1 code and checks did not need to change. |
| `control/GameEngine.java` | One line: takes the schedule from the config. | Follows from the `GameConfig` change. The turn loop itself is untouched. |
| `Main.java` | New `--schedule mixed\|regular` option (default `mixed`). | Lets a TA re-check Stage 1 behaviour. |
| `ui/ConsoleUI.java` | Banner says "Stage 2" and lists the arrivals with each type's loss per turn. | Display only. |
| `test/StageOneChecks.java` | The saved-log comparison now starts at "TURN 1" (the banner is skipped). | The banner now names the stage and lists arrivals; the game from turn 1 on must still be identical to the Stage 1 logs, and it is. |
| `README.md`, `DESIGN-NOTES.md`, `TESTING.md` | Updated for Stage 2. | Documentation. |

No files were deleted. `Restaurant`, `Chef`, `Cashier`, `Order`, `Table`, `Menu`, `MenuItem`,
`VictoryCondition`, `GameUI` and the turn steps are unchanged. The Cashier already charged the price
locked when the order was taken, so the VIP discount needed no change there.

## The Critic's special behaviour: signature dish

**A Critic always orders the most expensive dish on the menu** (Pasta, $18.00, 2 preparation
units, in the base menu), ignoring the seeded food-choice rule. On a price tie it takes the dish
listed first. The choice is displayed when the order is taken, like any other customer's.

Effect on strategy: the Critic loses satisfaction fastest (12 per turn) *and* always orders the
slowest dish, so a Critic left waiting drags the paid average down quickly. With full attention a
Critic pays at 64; every extra waiting turn costs 12 points. The Stage 2 runs show one such
decision deciding Victory or Defeat.

## What still works

- All 52 Stage 1 checks pass unchanged in behaviour (only the log comparison skips the banner).
- The Stage 1 victory and defeat scripts, replayed with `--schedule regular`, produce exactly the
  Stage 1 logs from turn 1 to the final result: same Victory (5 paid, $72.00, 82.4) and same Defeat
  (1 paid, $15.00, 28.0). Only the two banner lines differ.
- Invalid choices still explain why and do not use up the task; rules stay out of the UI; money is
  still integer cents.
- No Stage 3 features (Host, ComboMeal, Happy Hour).
