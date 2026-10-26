# Design Notes (final, Stage 3)

What changed in each stage: [CHANGES-stage1-to-2.md](CHANGES-stage1-to-2.md),
[CHANGES-stage2-to-3.md](CHANGES-stage2-to-3.md). Full class list for the class diagram:
[CLASS-DIAGRAM.md](CLASS-DIAGRAM.md).

## Packages and layers

| Package | Layer | Contents |
|---------|-------|----------|
| `restaurant.domain` | Entity | Game state and the rules that protect it: customers, orders, tables, staff, Happy Hour, pricing, restaurant |
| `restaurant.domain.menu` | Entity | `Menu`, `MenuItem`, `ComboMeal`. A separate package because they need no package-private access; customers and staff stay in `restaurant.domain` so their state-changing methods can remain package-private (encapsulation). |
| `restaurant.control` | Control | The turn loop, game setup per stage, arrival schedule, command objects, victory check |
| `restaurant.ui` | Boundary | `GameUI` interface and `ConsoleUI`; only collects decisions and displays state |
| `restaurant` | Entry | `Main` reads `--stage`, `--seed`, `--echo` and wires the objects together |

## Key abstractions and responsibilities

| Class | Kind | Responsibility |
|-------|------|----------------|
| `Customer` (abstract) | Entity | Satisfaction (kept in 0..100), status, table, order, key turns. Fixes the service flow. Overridable hooks: `getSatisfactionLossPerTurn()`, `getTypeName()`, `calculateBill(item)`, `chooseDish(menu, strategy)`. |
| `RegularCustomer` / `VIPCustomer` / `CriticCustomer` | Entity | Loss 8 / 5 / 12 per waiting turn; VIP 10% off; Critic always orders the most expensive item. |
| `CustomerStatus`, `OrderStatus` | Enums | Life cycles of a customer and of an order. |
| `Order` | Entity | Dish, **price locked when taken**, preparation progress, READY turn. |
| `MenuItem` | Entity | Name, price in cents, preparation units. |
| `ComboMeal` | Entity | A `MenuItem` made of two items: units = sum, price = 90% of the sum (nearest cent). |
| `Menu` | Entity | The dishes on offer: `standard()` (Stages 1-2) or `withCombos()` (Stage 3). |
| `Table` | Entity | Holds at most one customer until they pay or leave. |
| `Staff` (abstract) | Entity | One task per turn: `isAvailable()`, `resetForNewTurn()`, `waitThisTurn()`. |
| `Host` | Entity | Seats one queued customer per turn, before the Waiter acts. |
| `Waiter` | Entity | Seat / take order (with the locked price) / serve. |
| `Chef` | Entity | One preparation unit per turn, for any item including combos. |
| `Cashier` | Entity | Automatically takes one payment per turn, oldest READY_TO_PAY first. |
| `Promotion` (interface) | Entity | A time-limited discount: `getName()`, `isActive(turn)`, `applyDiscount(price)`. |
| `HappyHour` | Entity | The Stage 3 promotion. Owns its state: once per game, active for two turns, 20% off; defines the 15-point recovery. |
| `PricingPolicy` | Entity (policy) | The single place where the locked price is computed: customer discount first, then each active promotion. |
| `Restaurant` | Entity (aggregate) | Composed of tables, queue, Waiter, Chef, Cashier, optional Host, optional Happy Hour, pricing policy; holds the menu; keeps revenue; applies eating, waiting and Happy Hour recovery rules. |
| `ActionResult` | Value | Success flag plus message or failure reason. |
| `FoodChoiceStrategy` / `SeededRandomFoodChoice` | Strategy | The food-choice rule; seeded random for reproducible runs. |
| `Money` | Utility | Formats integer cents as dollars. |
| `GameEngine` | Control | Runs every turn in order; re-asks after invalid choices. |
| `GameConfig` | Control | Base setting plus the per-stage features (menu, Host, Happy Hour, arrivals) and the seed. |
| `ArrivalSchedule` (+ `Arrival`), `CustomerFactory` | Control | Which customer type arrives on which turn, created through constructor references. |
| `HostCommand`, `WaiterCommand`, `ChefCommand` | Control | The manager's parsed choice for each staff member. |
| `VictoryCondition`, `GameResult` | Control | Checks the three targets and explains each. |
| `GameUI` | Boundary (interface) | What the engine needs from any user interface. |
| `ConsoleUI` | Boundary | Text display and stdin input; handles typing errors only. |

## How a turn is organised (`GameEngine.playTurn`)

1. **Arrivals:** `ArrivalSchedule` creates the scheduled customer; `Restaurant.admitCustomer` queues them.
2. **Happy Hour offer (Stage 3):** only while `HappyHour.isAvailable()`, the UI asks yes/no; `yes` calls
   `HappyHour.activate(turn)`, which refuses a second activation with a reason.
3. **Happy Hour recovery (Stage 3):** `Restaurant.applyHappyHourRecovery(turn)` gives +15 (max 100) to
   every customer still waiting for a table or food, on each active turn.
   *Assumption:* activation and recovery happen after arrivals and before the start-of-turn display,
   so the turn on which it is switched on already counts as an active turn, and the manager sees the
   recovered values before choosing staff tasks. A customer who arrives on an active turn is already
   at 100.
4. **Start-of-turn display**, including Happy Hour status and remaining turns, and every staff task.
5. **Manager choices, Host → Waiter → Chef:** each is asked until valid; a failed `ActionResult` shows the
   reason and does not use the task. The Host acts first so the Waiter can take that customer's
   order in the same turn. When an order is taken, the customer chooses the dish (`chooseDish`) and
   `PricingPolicy.lockedPrice` fixes the price in the new `Order`.
6. **Eating and payment:** served on an earlier turn → READY_TO_PAY; the Cashier pays one customer who
   became READY_TO_PAY on an earlier turn, using the locked price.
7. **Waiting and abandonment:** everyone still waiting for food loses their type's amount; at 0 they
   leave, their order is cancelled and their table freed.
8. After turn 18, `VictoryCondition` decides Victory or Defeat.

## Object-oriented concepts (where they genuinely appear)

**Encapsulation**
- All fields private, no public setters. State changes on `Customer`, `Order` and `Table` are
  package-private and called only by the domain classes that check the rules.
- `satisfaction` is private to `Customer`; `loseSatisfaction` and `recoverSatisfaction` clamp to 0..100.
- `HappyHour` keeps `activatedOnTurn` private. The once-per-game rule and the duration are decided
  inside `activate` / `isActive`; the UI only asks yes/no and displays `remainingTurns`.
- The price is locked in `Order.priceCents` (final) when the order is taken, so a later Happy Hour,
  or its end, can never change it. Revenue only changes inside `Restaurant.recordPayment`.

**Inheritance and abstraction**
- `Customer` → `RegularCustomer`, `VIPCustomer`, `CriticCustomer`.
- `Staff` → `Waiter`, `Chef`, `Cashier`, `Host` (the Host reused the one-task-per-turn rule with no change to `Staff`).
- `MenuItem` → `ComboMeal` (a combo *is* a menu item: the Chef, `Order`, `Menu`, pricing and the UI
  handle it with no special case).

**Polymorphism**
- Customer hooks: loss per turn (8/5/12), bill (VIP), dish choice (Critic) and type name, called by
  `Restaurant`, `Waiter` and `PricingPolicy` through `Customer` references.
- `PricingPolicy` calls `Promotion.isActive` / `applyDiscount` without knowing it is a `HappyHour`.
- `ComboMeal` is used wherever a `MenuItem` is expected (`Order.getItem().getPrepUnits()` gives 3 for
  Salad + Pasta, so `Chef.prepare` needs no change).
- `GameEngine` → `GameUI` (`ConsoleUI`, `ScriptedUI`, `GreedyUI`) and `FoodChoiceStrategy`.
- No `instanceof`, `getClass()` or type switch anywhere in `src` (checked by `StageTwoChecks`).

**Composition**
- `Restaurant` creates and owns its `Table`s, `Waiter`, `Chef`, `Cashier`, the optional `Host` and
  `HappyHour`, and the `PricingPolicy`; it holds the `Menu`.
- `GameEngine` owns the `Restaurant` and the `VictoryCondition`.
- `ComboMeal` is built from two `MenuItem`s; `ArrivalSchedule` from `Arrival`s.

## SOLID (only where genuinely reflected)

- **SRP:** pricing order lives only in `PricingPolicy`; Happy Hour's timing rules only in `HappyHour`;
  the seating rule only in `Restaurant.whyCannotSeat` (shared by `Host` and `Waiter`); which features
  a stage has only in `GameConfig`; display only in `ConsoleUI`; the turn order only in `GameEngine`.
- **OCP:** Stage 3 shows it in three places. (1) Combos: a new `MenuItem` subclass; `Chef`, `Order`,
  `Cashier` and `Menu`'s users did not change. (2) The Host: a new `Staff` subclass; `Staff` did not
  change. (3) Pricing: a new promotion would be a new `Promotion` class added to the list;
  `PricingPolicy` and `Waiter` would not change. Honest limits: Stage 3 needed small, explained
  edits to `Waiter` (to use `PricingPolicy`), `Restaurant` (to own the new parts), `GameEngine` (two
  new turn steps) and `GameUI` (two new questions), because these are genuinely new rules and decisions.
- **LSP:** every `Customer` subclass, `ComboMeal` for `MenuItem`, `Host` for `Staff` and the test UIs
  for `GameUI` are substitutable: the base class fixes the flow and the invariants (satisfaction
  0..100, one task per turn, status checks), and subclasses only supply values.
- **DIP:** `GameEngine` depends on `GameUI` and `FoodChoiceStrategy`; `PricingPolicy` on `Promotion`,
  not on `HappyHour`; `ArrivalSchedule` on `CustomerFactory`. `Main` and `Restaurant` choose the
  concrete classes.
- **ISP:** not claimed. `GameUI` is still one interface and the test UIs leave some display methods
  empty. Splitting it (e.g. input vs output) was considered, but the only client, `GameEngine`, needs
  all of it, so it would add types without a real benefit.

## Decisions where the brief left a choice

- **Host vs Waiter order:** the Host acts first, then the Waiter, then the Chef.
- **Happy Hour timing:** offered after arrivals while unused; recovery applies on each active turn
  before the display; activated on turn *t*, it is active on *t* and *t+1* (turn 18 only if activated on 18).
- **Happy Hour recovery applies to** WAITING, SEATED and ORDERED customers (waiting for a table or
  food), including those whose dish is READY but not served. Served/eating/ready-to-pay and departed
  customers are not affected.
- **Combo rule:** the three combos of two different base dishes. Customers choose with the same seeded
  random rule from all six items (combo with probability 1/2); the Critic picks the most expensive
  item, which is now the Burger + Pasta Combo.
- **Rounding:** each discount step rounds to the nearest cent, half a cent up. Because of this the order
  matters (35 cents: VIP then Happy Hour = 26 cents; the reverse would be 25), and the brief's order is used.
- **Stages stay runnable:** `--stage 1|2|3` selects the stage's features; Stage 3 is the default.
- Earlier decisions still hold: any queued customer may be seated (lowest free table); a READY but
  unserved order is cancelled if its customer leaves; Cashier ties go to the earlier arrival.

## Trade-offs

- **Optional Host/Happy Hour inside `Restaurant` (null + `Optional` getters):** keeps one `Restaurant`
  class for all stages, so Stage 1 and 2 behaviour is provably unchanged, at the cost of two nullable
  fields. A Stage 3 subclass of `Restaurant` was rejected because the engine would need to know
  which one it has.
- **`Promotion` interface with only one implementation today:** it is a real seam (pricing depends on
  "some active discounts", not on Happy Hour), but it is honest to say there is one promotion now.
- **Bill label built by `PricingPolicy.describeDiscounts`:** keeps the "VIP + Happy Hour bill" text
  consistent with the price calculation, at the cost of one display-oriented method in a domain class.
- **Validate-then-execute each staff choice immediately** (Host, then Waiter, then Chef): same outcome
  as collecting all choices first, because the order is fixed, and an invalid choice is re-asked at once.
- **Combo as a subclass of `MenuItem` vs. an order holding two items:** the subclass means every
  existing class treats a combo as one dish with more units; the cost is that a combo's components are
  only informational (they are not cooked separately), which matches the brief.

## Alternative design considered and rejected

**Happy Hour as a flag in `GameEngine`, with `if` checks in the Waiter and the UI.** For example a
`boolean happyHourActive` and `int happyHourTurnsLeft` in the engine, the Waiter computing
`if (happyHourActive) price = price * 80 / 100` after the VIP discount, and the console counting the
turns down. It is fewer classes, but:
- the once-per-game and two-turn rules would be spread over the engine and the UI (the brief asks for
  this state to live in the class that owns it);
- the discount order (VIP first, then Happy Hour) would be hidden inside the Waiter, mixed with order
  taking;
- every future promotion would add another flag and another `if` in several classes (violating OCP).

We chose `HappyHour` (owns its state and rules) + `PricingPolicy` (owns the stacking order) +
`Promotion` (the seam between them) instead.

## How the player's choices change the result

The Stage 3 runs (`runs/stage3-victory.txt` / `stage3-defeat.txt`, seed 2002) are identical except for
one answer: switching Happy Hour on at turn 10. With it, the waiting Critic recovers 15 on turn 10
(28 → 43, just before being served), the VIP and C4 recover on turns 10 and 11, and the VIP's Burger
is locked at $10.80: revenue falls from $89.10 to $86.40,
but the paid average rises from 53.8 to 64.5 and the game is won. Without it, the same game is lost
on satisfaction. Earlier stages show the same idea with other choices: serving the Critic before a
Regular (Stage 2), and fast versus slow service (Stage 1).
