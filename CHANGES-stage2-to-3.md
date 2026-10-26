# Changes from Stage 2 to Stage 3

Stage 3 adds the Host, ComboMeal and Happy Hour. The six turn steps, the customer types and all
Stage 1 and 2 rules are unchanged. Earlier stages stay playable with `--stage 1` and `--stage 2`.

Compare link (after the tags are pushed):
https://github.com/crocmii08-creator/2002-own-/compare/stage-2...stage-3

## New files

| File | Purpose |
|------|---------|
| `src/restaurant/domain/Host.java` | `Host extends Staff`: seats at most one queued customer per turn, in addition to the Waiter's task. |
| `src/restaurant/domain/menu/ComboMeal.java` | `ComboMeal extends MenuItem`: two existing items; units = sum, price = 90% of the combined price (nearest cent). |
| `src/restaurant/domain/Promotion.java` | Interface for a time-limited discount: `getName()`, `isActive(turn)`, `applyDiscount(price)`. |
| `src/restaurant/domain/HappyHour.java` | `HappyHour implements Promotion`: once-per-game activation, two-turn duration, 20% off, 15-point recovery constant. Owns its own state. |
| `src/restaurant/domain/PricingPolicy.java` | Computes the price locked into an order: customer discount first (VIP), then each active promotion. |
| `src/restaurant/control/HostCommand.java` | The manager's Host choice: `seat <id>` or `wait`. |
| `test/restaurant/StageThreeChecks.java` | Stage 3 automated checks (see TESTING.md). |
| `runs/stage3-victory.txt` / `.log`, `runs/stage3-defeat.txt` / `.log` | Reproducible Stage 3 runs (same seed, one different choice). |
| `CHANGES-stage2-to-3.md`, `CLASS-DIAGRAM.md` | This file; the final class list for the class diagram. |

## Existing files changed

| File | Change | Why |
|------|--------|-----|
| `domain/Restaurant.java` | Also owns an optional `Host`, an optional `HappyHour` and a `PricingPolicy` (new 4-argument constructor; the old 2-argument one still means "no Host, no Happy Hour"). New `applyHappyHourRecovery(turn)`, `getHost()`, `getHappyHour()`, `getPricingPolicy()`. `whyCannotSeat` moved here from `Waiter`. `getStaff()` includes the Host when present. | Composition: the restaurant owns the new staff member and the promotion, so the Happy Hour state and its once-per-game rule live in the domain, not the UI. Moving `whyCannotSeat` lets Host and Waiter share one seating rule instead of copying it. |
| `domain/Waiter.java` | `takeOrder` gets the locked price from `restaurant.getPricingPolicy()` instead of `customer.calculateBill(...)`; the bill note now names all discounts (e.g. "VIP + Happy Hour bill: $10.80"). The private seating check was removed (now in `Restaurant`). | Discounts stack, so the order in which they apply must be decided in one place. With no promotion active the price and the message are exactly as in Stage 2. |
| `domain/Customer.java` | New package-private `recoverSatisfaction(points)`, capped at 100. | Happy Hour recovery; satisfaction stays private and in 0..100. |
| `domain/Menu.java` → `domain/menu/Menu.java` | New `Menu.withCombos()` (3 items + 3 combos). `Menu.standard()` unchanged. | Stage 3 menu. |
| `domain/MenuItem.java` → `domain/menu/MenuItem.java`, plus `ComboMeal` | `Menu`, `MenuItem` and `ComboMeal` moved into the sub-package `restaurant.domain.menu`; every class that uses them now imports it. No code inside them changed. | Groups the menu classes in their own folder. None of their members is package-private, so the move needed no visibility change. The customer and staff classes stay in `restaurant.domain`, because they call each other's package-private methods (e.g. `Customer.markPaid()`), and making those public would let any class change state without the rule checks. |
| `control/GameConfig.java` | New components `stage`, `menu`, `hostEnabled`, `happyHourEnabled`; new `stageThree(...)` and `forStage(n)`. `baseSetting(seed)` (Stage 1) and `baseSetting(seed, schedule)` (Stage 2) keep their meaning. | The features are switched on per stage, so Stage 1 and 2 games (and their input scripts) are unchanged. |
| `control/GameEngine.java` | Builds the `Restaurant` from the config's menu and switches. At turn start: offers Happy Hour while it is unused, then applies the recovery. Runs the Host's task before the Waiter's. | The two new turn-start steps and the Host. The other steps are untouched. |
| `ui/GameUI.java` | New `askHappyHour()` and `askHostCommand()`; `showTurnStart` also receives the turn and the Host's tasks. | The engine needs these two new decisions from the player. |
| `ui/ConsoleUI.java` | Implements the new methods; banner shows the stage number, Host commands and Happy Hour rules (with blank lines between sections); the start-of-turn display shows Happy Hour status and remaining turns and the Host's tasks (only when the game has them). | Display only. |
| `Main.java` | `--stage 1\|2\|3` (default 3) replaces `--schedule`. | Selects a whole stage, not just the arrivals. |
| `test/ScriptedUI.java`, `test/GreedyUI.java` | Implement the new `GameUI` methods. `GreedyUI` now reads the Waiter's tasks when asked (the Host may have just seated someone). | Test UIs must match the interface. For Stages 1 and 2 the chosen commands are the same as before. |
| `README.md`, `DESIGN-NOTES.md`, `TESTING.md` | Updated for Stage 3. | Documentation. |

No files were deleted (`Menu` and `MenuItem` were moved, see above). Unchanged apart from comments
and, where needed, an import of `restaurant.domain.menu`: `Order`, `Table`, `Staff`, `Chef`,
`Cashier`, `RegularCustomer`, `VIPCustomer`, `CriticCustomer`, `ArrivalSchedule`, `CustomerFactory`,
`VictoryCondition`, `SeededRandomFoodChoice`, `StageOneChecks`, `StageTwoChecks`. The Chef needed no
change for combos (it already works in preparation units), and the Cashier none for Happy Hour (it
already collects the price locked in the order).

## Stage 3 rules as implemented

- **Turn start order:** (1) arrivals → (2) Happy Hour offer, only while unused → (3) on an active
  turn, every customer still waiting for a table or food (WAITING, SEATED, ORDERED) recovers 15,
  max 100 → (4) start-of-turn display → (5) Host, then Waiter, then Chef → eating/payment → waiting
  penalties, as before. A customer who arrives on the activation turn is already at 100, so gains
  nothing; served customers and customers who have left are not affected.
- **Duration:** activated on turn *t*, active on *t* and *t+1* (on turn 18 only turn 18 remains).
  The display shows "2 turns left (this turn and next)", then "1 turn left (last turn)".
- **Host before Waiter:** the Host seats first, so the Waiter can take that customer's order in the
  same turn. The Waiter can still seat customers itself.
- **Combo choice rule:** the existing seeded random rule picks uniformly from all six menu items,
  so a Regular or VIP orders a combo with probability 1/2. The Critic's signature-dish rule picks the
  most expensive item, which in Stage 3 is the Burger + Pasta Combo ($29.70, 3 units).
- **Pricing:** customer discount, then Happy Hour, rounding to the nearest cent at each step; the
  result is locked into the `Order` when it is taken.

## What still works

- `StageOneChecks` (52) and `StageTwoChecks` (38) pass unchanged.
- `--stage 2` replays both Stage 2 scripts with exactly the same turns, choices and results as the
  Stage 2 build. The welcome banner was restyled in Stage 3 (wider title, blank lines between
  sections), so the two Stage 2 logs in `runs/` were re-recorded; the Stage 2 originals are kept at
  the `stage-2` tag, and comparing them shows only those banner lines differ.
- `--stage 1` replays both Stage 1 scripts: identical from turn 1 to the final result (only the
  banner lines before turn 1 differ).
- Invalid choices (including Host choices and Happy Hour answers) explain why and do not use up the
  task; rules stay in the domain; money is integer cents; customer behaviour is still dispatched
  through `Customer` subclasses with no type checks (the Stage 2 source scan still passes).
