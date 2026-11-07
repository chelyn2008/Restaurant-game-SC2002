# Changes from Stage 2 to Stage 3

Stage 3 adds the Host, ComboMeal and Happy Hour. The six turn steps, the customer types and all
Stage 1 and 2 rules are unchanged. Earlier stages stay playable with `--stage 1` and `--stage 2`.

Compare link (after the tags are pushed):
https://github.com/chelyn2008/Restaurant-game-SC2002/compare/stage-2...stage-3

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
| `src/restaurant/domain/ServiceAdvisor.java` | **Extra feature** (`--hints`): turns until each waiting customer leaves, the most urgent customer, and their next step. Only reads the game state. |
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

## Display improvement: tasks shown again during the turn

The start-of-turn display lists what each staff member can do, but the Host acts before the Waiter
and the Waiter before the Chef, so an earlier action can open a new task (the Host seats C1, so the
Waiter can now take C1's order; the Waiter takes an order, so the Chef can now cook it, as section 3.2
allows). Those commands were always accepted, because every command is checked against the current
state, but the earlier list did not show them. Now the changed list is printed just before the next
prompt, e.g. `Chef can now: cook C1 | wait`. Nothing is printed when the list did not change.
Because the turn loop is shared by every stage, `--stage 1` and `--stage 2` games show these lines
too (after the Waiter takes an order); no rule changes.

| File | Change | Why |
|------|--------|-----|
| `control/GameEngine.java` | `playTurn` keeps the start-of-turn lists; after the Host and the Waiter act, it asks the next staff member's `availableTasks` again and, if the list changed, sends it through the existing `GameUI.showMessage` | The lists still come from the domain classes, so no rule changed. No new method or interface change, so the class diagrams are unaffected (trade-off: the short "only if changed" check appears twice in `playTurn`). |
| `runs/*.log` (all six) | Re-recorded | Only the new "can now" lines were added (42 lines, none removed). |
| `test/restaurant/StageThreeChecks.java` | New section `updatedTasksShownDuringTurn` (3 checks) | See TESTING.md. |

## Extra feature: leave warnings and hints (`--hints`)

Added on top of the Stage 3 requirements, as allowed by section 4.4 of the brief. It is **off by
default**: without `--hints` the game, its output and every saved run are exactly as before.

| File | Change | Why |
|------|--------|-----|
| `domain/ServiceAdvisor.java` (new) | `turnsUntilLeaving`, `leavingTurn`, `mostUrgent`, `nextStep` (names the staff member, e.g. "Chef: cook C2"; seating names the Host only when the game has one) | The calculation is a game rule (it mirrors the abandonment rule), so it belongs in the domain, not the UI. It calls `Customer.getSatisfactionLossPerTurn()`, so each customer type gives its own answer with no type checks. |
| `control/GameConfig.java` | New component `hintsEnabled` (false in every stage) and `withHints()` | The feature is switched on per game, like the Host and Happy Hour. |
| `control/GameEngine.java` | Shows a warning per waiting customer after the start-of-turn display (if the leaving turn is after turn 18, it says the customer "will not leave before the game ends" instead); the Host/Waiter/Chef loops answer `hint` and ask again without using the task; `runHostTask` now also takes the turn | Same "ask again, task not used" rule as for invalid choices. Uses the existing `GameUI.showMessage`, so `GameUI` is unchanged. |
| `control/HostCommand.java`, `WaiterCommand.java`, `ChefCommand.java`, `CommandParsing.java` | New `HINT` action, parsed from the single word `hint` | `hint` can be typed at any staff prompt. The usage messages are unchanged. |
| `ui/ConsoleUI.java` | One banner line, only when hints are on | Display only. |
| `Main.java` | `--hints` option | Lets the player switch the feature on. |
| `control/CommandParsing.java` | Also: an id too long for an `int` (e.g. `c99999999999`) now gives "'c99999999999' is not a customer id like C1." instead of Java's raw "For input string" message | Small input-handling fix found while testing; it was already rejected and re-asked, only the reason was unclear. |
| `test/restaurant/StageThreeChecks.java` | New section `extraLeaveWarningsAndHints` (13 checks) | See TESTING.md. |

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

- `StageOneChecks` (52) and `StageTwoChecks` (38) pass unchanged; `StageThreeChecks` passes 64
  (48 for the required Stage 3 rules, 13 for the extra feature and the input fix, 3 for the
  "can now" display). All six saved logs were re-recorded for the "can now" lines only.
- `--stage 2` replays both Stage 2 scripts with exactly the same turns, choices and results as the
  Stage 2 build. The welcome banner was restyled in Stage 3 (wider title, blank lines between
  sections), so the two Stage 2 logs in `runs/` were re-recorded; the Stage 2 originals are kept at
  the `stage-2` tag, and comparing them shows only those banner lines differ.
- `--stage 1` replays both Stage 1 scripts with exactly the same turns, choices and results as the
  Stage 1 build. Compared with the logs at the `stage-1` tag, only the banner lines before turn 1
  differ and the new `Chef can now: ...` lines are added (6 in the victory run, 2 in the defeat run).
- Invalid choices (including Host choices and Happy Hour answers) explain why and do not use up the
  task; rules stay in the domain; money is integer cents; customer behaviour is still dispatched
  through `Customer` subclasses with no type checks (the Stage 2 source scan still passes).
