# Design Notes (Stage 2)

Stage 2 adds VIP and Critic customers. What changed since Stage 1 is listed in
[CHANGES-stage1-to-2.md](CHANGES-stage1-to-2.md).

## Packages and layers

| Package | Layer | Contents |
|---------|-------|----------|
| `restaurant.domain` | Entity | Game state and the rules that protect it: customers, orders, tables, menu, staff, restaurant |
| `restaurant.control` | Control | The turn loop, game setup, arrival schedule, command objects, victory check |
| `restaurant.ui` | Boundary | `GameUI` interface and `ConsoleUI`; only collects decisions and displays state |
| `restaurant` | Entry | `Main` wires the objects together |

## Key classes and responsibilities

| Class | Kind | Responsibility |
|-------|------|----------------|
| `Customer` (abstract) | Entity | Satisfaction, status, table, order and the turns it was served / became READY_TO_PAY. Fixes the service flow. Defines the overridable hooks `getSatisfactionLossPerTurn()`, `getTypeName()`, `calculateBill(MenuItem)` and `chooseDish(Menu, FoodChoiceStrategy)`. |
| `RegularCustomer` | Entity | Loses 8 per waiting turn, full price, follows the food-choice rule. |
| `VIPCustomer` | Entity | Loses 5 per waiting turn, 10% off the bill (nearest cent), follows the food-choice rule. |
| `CriticCustomer` | Entity | Loses 12 per waiting turn, full price, always orders the most expensive dish. |
| `CustomerStatus`, `OrderStatus` | Enums | The life cycle of a customer (WAITING ... PAID / LEFT) and of an order (PLACED ... PAID / CANCELLED). |
| `Order` | Entity | Dish, price locked when taken, preparation progress, the turn it became READY. |
| `MenuItem`, `Menu` | Entity | Dish name, price in cents, preparation units; the list of dishes. |
| `Table` | Entity | Holds at most one customer until they pay or leave. |
| `Staff` (abstract) | Entity | Shared "one task per turn" rule: `isAvailable()`, `resetForNewTurn()`, `waitThisTurn()`. |
| `Waiter` | Entity | Validates and performs seat / take order / serve; lists its available tasks. |
| `Chef` | Entity | Validates and performs one preparation unit. |
| `Cashier` | Entity | Automatically takes one payment per turn, oldest READY_TO_PAY first. |
| `Restaurant` | Entity (aggregate) | Composed of tables, waiting queue, staff and menu; keeps revenue; applies eating and waiting/abandonment rules; answers statistics queries. |
| `ActionResult` | Value | Success flag plus a message or failure reason. |
| `FoodChoiceStrategy` / `SeededRandomFoodChoice` | Strategy | The game's food-choice rule; seeded random for reproducible runs. |
| `Money` | Utility | Formats integer cents as dollars. |
| `GameEngine` | Control | Runs the six turn steps for all 18 turns; asks again after an invalid choice. |
| `GameConfig` | Control | Base setting values, the arrival schedule and the seed. |
| `ArrivalSchedule` (+ `Arrival` record) | Control | Which customer type arrives on which turn; creates them (C1..C6). `allRegular()` and `stageTwoMix()`. |
| `CustomerFactory` | Control (interface) | Creates one kind of customer; implemented by constructor references. |
| `WaiterCommand`, `ChefCommand` | Control | The manager's parsed choice for each staff member. |
| `VictoryCondition`, `GameResult` | Control | Checks the three targets and explains each one. |
| `GameUI` | Boundary (interface) | What the engine needs from any user interface. |
| `ConsoleUI` | Boundary | Text display and stdin input; handles typing errors only. |

## Customer types

| Type | Loss per waiting turn | Bill | Special behaviour |
|------|----------------------|------|-------------------|
| Regular | 8 | Full price | — |
| VIP | 5 | 10% off, rounded to the nearest cent: `(price × 90 + 50) / 100` in cents ($8.10 / $13.50 / $16.20) | — |
| Critic | 12 | Full price | **Signature dish:** always orders the most expensive dish on the menu (Pasta), ignoring the food-choice rule; first listed on a price tie. |

All three follow the same flow (queue → seat → order → cook → READY → SERVED → eat →
READY_TO_PAY → PAID, or LEFT at satisfaction 0), because the flow is implemented once in `Customer`.

## How a turn is organised (`GameEngine.playTurn`)

1. `ArrivalSchedule` creates the scheduled customer through its `CustomerFactory`;
   `Restaurant.admitCustomer` adds them to the queue. The UI shows the start-of-turn state and the
   tasks returned by `Waiter.availableTasks` / `Chef.availableTasks`.
2. and 3. The engine asks for a Waiter command and calls the matching `Waiter` method; if the
   `ActionResult` fails, the reason is shown and the manager is asked again. Then the same for the Chef.
   When taking an order the Waiter calls `customer.chooseDish(...)` and `customer.calculateBill(...)`,
   so the dish and the locked price come from the customer object.
   Because the Waiter acts before the Chef, a dish finished this turn cannot be served this turn;
   `Waiter` also checks this explicitly (`readyOnTurn < currentTurn`).
4. `Restaurant.finishEating` moves customers served on an earlier turn to READY_TO_PAY; then
   `Cashier.collectPayment` pays at most one customer who became READY_TO_PAY on an earlier turn.
5. `Restaurant.applyWaitingPenalties` calls `loseSatisfaction()` on everyone still waiting for food;
   the amount comes from each object's `getSatisfactionLossPerTurn()`. Customers at 0 leave, their
   order is cancelled and their table freed.
6. After the last turn, `VictoryCondition.evaluate` decides Victory or Defeat.

## Object-oriented concepts in the code

**Encapsulation**
- All fields are private and there are no public setters. State changes on `Customer`, `Order` and
  `Table` (`seatAt`, `placeOrder`, `markServed`, `markPaid`, `leave`, `loseSatisfaction`, `assign`,
  `release` ...) are package-private, so only the domain classes that check the rules (`Waiter`,
  `Chef`, `Cashier`, `Restaurant`) can call them.
- `satisfaction` is private to `Customer`. Subclasses cannot set it; they only say *how much* is lost
  per turn. The base class applies the loss and clamps at 0, so no subclass can push satisfaction out
  of 0..100.
- Each transition checks the current status and throws if called out of order (e.g. `markPaid`
  requires READY_TO_PAY).
- `Restaurant` returns unmodifiable lists; `ArrivalSchedule` and `GameConfig` are immutable.
- Money is integer cents; revenue only changes inside `Restaurant.recordPayment`, using the price
  locked in the `Order` (so a VIP's discount is fixed when the order is taken).

**Inheritance and abstraction**
- `Customer` (abstract) → `RegularCustomer`, `VIPCustomer`, `CriticCustomer`.
- `Staff` (abstract) → `Waiter`, `Chef`, `Cashier`, sharing the one-task-per-turn logic.

**Polymorphism** (now with three real implementations)
- `Restaurant.applyWaitingPenalties` → `customer.loseSatisfaction()` → overridden
  `getSatisfactionLossPerTurn()`: 8, 5 or 12.
- `Waiter.takeOrder` → `customer.calculateBill(item)`: full price, or 10% off for a VIP.
- `Waiter.takeOrder` → `customer.chooseDish(menu, strategy)`: the strategy for Regular and VIP, the
  most expensive dish for a Critic.
- `ConsoleUI` and the engine's messages → `getTypeName()`.
- There is no `instanceof`, `getClass()` or type switch anywhere in `src`; a check scans for it.
- `GameEngine` → `GameUI` (`ConsoleUI`, or `ScriptedUI` / `GreedyUI` in checks) and
  `FoodChoiceStrategy` (`SeededRandomFoodChoice`, or `FixedFoodChoice` in checks).
- `ArrivalSchedule` → `CustomerFactory.create(...)`, implemented by constructor references.

**Composition**
- `Restaurant` creates and owns its `Table`s, `Waiter`, `Chef`, `Cashier` and the waiting queue, and
  holds the `Menu`.
- `GameEngine` owns the `Restaurant` and `VictoryCondition`, and uses the `ArrivalSchedule` from its
  `GameConfig`. An `ArrivalSchedule` is made of `Arrival`s, each holding a `CustomerFactory`.
- A `Customer` has an `Order`; an `Order` refers to a `MenuItem`.

## SOLID principles (only where they genuinely appear)

- **SRP:** each class has one reason to change. VIP pricing lives only in `VIPCustomer`, the Critic's
  order preference only in `CriticCustomer`, which customers arrive only in `ArrivalSchedule`, display
  only in `ConsoleUI`, the win targets only in `VictoryCondition`, the turn order only in `GameEngine`.
- **OCP (shown in practice by Stage 2):** adding two customer types needed two new subclasses plus
  registering them in `ArrivalSchedule`. `GameEngine`'s turn loop, `Restaurant`, `Chef`, `Cashier`
  and `VictoryCondition` were not changed. Two small edits were needed and are explained in
  CHANGES-stage1-to-2.md: one new hook on `Customer` (`chooseDish`, needed for the Critic) and the
  Waiter calling that hook. With the hook in place, a further type with its own order preference
  would need no edit outside its own class and the schedule.
- **LSP:** every subclass can stand in for `Customer`: the service flow, the status checks and the
  0..100 range are enforced in the base class, and subclasses only return values (a loss amount,
  a bill, a dish from the menu). Checks run the unchanged engine with all three types and confirm
  satisfaction stays in 0..100 and that a Critic at 0 leaves exactly like a Regular.
  `ScriptedUI` / `GreedyUI` likewise replace `ConsoleUI` without changing engine behaviour.
- **DIP:** `GameEngine` depends on the `GameUI` and `FoodChoiceStrategy` abstractions, and
  `ArrivalSchedule` on `CustomerFactory`; `Main` (or a test) chooses the concrete classes.
- **ISP:** not claimed. `GameUI` is one interface and the test UIs leave some display methods empty;
  we kept one interface because the engine needs all of it. (`CustomerFactory` and
  `FoodChoiceStrategy` are one-method interfaces, but that is just their size, not a split.)

## Decisions where the brief left a choice

- **Seating:** the Waiter may seat any queued customer (not only the first); they go to the
  lowest-numbered free table.
- **Food choice:** Regular and VIP customers choose when the order is taken, using `java.util.Random`
  with the game seed (default 2002, `--seed N`). The Critic does not use the random rule, so a
  Critic's order does not use up a random number.
- **Critic special behaviour:** signature dish, as above. We considered two alternatives: a reaction
  to service (bonus or penalty at serving depending on wait) and a lower walk-out threshold (leave at
  40 instead of 0). We chose the order preference because it is deterministic, easy to see in the
  output, and combines with the 12-point loss to make the Critic the customer to serve first.
- **VIP rounding:** half a cent rounds up. With the base menu no rounding is actually needed.
- **Stage 2 arrivals (`stageTwoMix`):** Regular (1), Critic (4), VIP (7), Regular (10, 13, 16).
  `--schedule regular` restores the Stage 1 all-Regular arrivals.
- **Leaving with a READY order:** the order is cancelled; it can no longer be served and never counts
  as revenue.
- **"Oldest first" for the Cashier:** the customer who became READY_TO_PAY earliest pays first;
  ties go to the earlier arrival.
- **Typing errors** are handled by the UI; **rule errors** come from the domain as an `ActionResult`
  reason. Neither uses up the task.

## Prepared for Stage 3 (not implemented)

- `Order` locks its price when the order is taken (Happy Hour will need this).
- `MenuItem` is a class rather than an enum, so a combo can be modelled as a menu item.
- `Staff` base class for a new staff role.

## Trade-offs and an alternative considered

- **Type enum + switch (rejected):** a `CustomerType` enum with `switch` statements for loss, bill and
  dish would be fewer files, but every new type would mean editing the engine/Waiter, and Stage 2
  requires behaviour to be dispatched through the customer objects.
- **Critic preference as a hook vs. a per-customer strategy object:** we added `chooseDish` to
  `Customer` rather than giving each customer its own `FoodChoiceStrategy`. It is one method, easy to
  explain, and keeps "what this customer likes" with the customer. The cost is one small change to
  `Customer` and `Waiter` in Stage 2.
- **Factory references vs. reflection or a type name string:** `ArrivalSchedule` stores constructor
  references (`VIPCustomer::new`), which the compiler checks, instead of strings like `"VIP"` that
  would need a lookup table or a switch.
- **Rules inside staff classes vs. one big rules class:** each staff member's checks stay in that
  staff class. This keeps each class small, at the cost of the staff needing a reference to the
  `Restaurant` for some checks.
- **Validate-then-execute immediately:** each staff choice is validated and executed as soon as it is
  made (Waiter first, then Chef). The outcome is the same as collecting both first, because the
  Waiter always acts before the Chef, and it lets an invalid choice be re-asked straight away.
