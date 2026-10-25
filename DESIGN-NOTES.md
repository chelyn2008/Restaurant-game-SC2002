# Design Notes (Stage 1)

## Packages and layers

| Package | Layer | Contents |
|---------|-------|----------|
| `restaurant.domain` | Entity | Game state and the rules that protect it: customers, orders, tables, menu, staff, restaurant |
| `restaurant.control` | Control | The turn loop, game setup, arrivals, command objects, victory check |
| `restaurant.ui` | Boundary | `GameUI` interface and `ConsoleUI`; only collects decisions and displays state |
| `restaurant` | Entry | `Main` wires the objects together |

## Key classes and responsibilities

| Class | Kind | Responsibility |
|-------|------|----------------|
| `Customer` (abstract) | Entity | Satisfaction, status, table, order and the turns it was served / became READY_TO_PAY. Defines the overridable hooks `getSatisfactionLossPerTurn()`, `getTypeName()` and `calculateBill(MenuItem)`. |
| `RegularCustomer` | Entity | Loses 8 satisfaction per waiting turn, pays full price. |
| `CustomerStatus`, `OrderStatus` | Enums | The life cycle of a customer (WAITING ... PAID / LEFT) and of an order (PLACED ... PAID / CANCELLED). |
| `Order` | Entity | Dish, locked price, preparation progress, the turn it became READY. |
| `MenuItem`, `Menu` | Entity | Dish name, price in cents, preparation units; the list of dishes. |
| `Table` | Entity | Holds at most one customer until they pay or leave. |
| `Staff` (abstract) | Entity | Shared "one task per turn" rule: `isAvailable()`, `resetForNewTurn()`, `waitThisTurn()`. |
| `Waiter` | Entity | Validates and performs seat / take order / serve; lists its available tasks. |
| `Chef` | Entity | Validates and performs one preparation unit. |
| `Cashier` | Entity | Automatically takes one payment per turn, oldest READY_TO_PAY first. |
| `Restaurant` | Entity (aggregate) | Composed of tables, waiting queue, staff and menu; keeps revenue; applies eating and waiting/abandonment rules; answers statistics queries. |
| `ActionResult` | Value | Success flag plus a message or failure reason. |
| `FoodChoiceStrategy` / `SeededRandomFoodChoice` | Strategy | How a customer picks a dish; seeded random for reproducible runs. |
| `Money` | Utility | Formats integer cents as dollars. |
| `GameEngine` | Control | Runs the six turn steps for all 18 turns; asks again after an invalid choice. |
| `GameConfig` | Control | Base setting values and the seed. |
| `ArrivalSchedule` | Control | Creates the customer arriving on a turn (C1..C6). |
| `WaiterCommand`, `ChefCommand` | Control | The manager's parsed choice for each staff member. |
| `VictoryCondition`, `GameResult` | Control | Checks the three targets and explains each one. |
| `GameUI` | Boundary (interface) | What the engine needs from any user interface. |
| `ConsoleUI` | Boundary | Text display and stdin input; handles typing errors only. |

## How a turn is organised (`GameEngine.playTurn`)

1. `ArrivalSchedule` creates the scheduled customer; `Restaurant.admitCustomer` adds them to the queue.
   The UI shows the start-of-turn state and the tasks returned by `Waiter.availableTasks` / `Chef.availableTasks`.
2. and 3. The engine asks for a Waiter command and calls the matching `Waiter` method; if the
   `ActionResult` fails, the reason is shown and the manager is asked again. Then the same for the Chef.
   Because the Waiter acts before the Chef, a dish finished this turn cannot be served this turn;
   `Waiter` also checks this explicitly (`readyOnTurn < currentTurn`).
4. `Restaurant.finishEating` moves customers served on an earlier turn to READY_TO_PAY; then
   `Cashier.collectPayment` pays at most one customer who became READY_TO_PAY on an earlier turn.
5. `Restaurant.applyWaitingPenalties` reduces satisfaction of everyone still waiting for food
   (queue, seated, ordered including READY-but-unserved); customers at 0 leave, their order is
   cancelled and their table freed.
6. After the last turn, `VictoryCondition.evaluate` decides Victory or Defeat.

## Object-oriented concepts in the code

**Encapsulation**
- All fields are private. There are no public setters. State changes on `Customer`, `Order` and `Table`
  (`seatAt`, `placeOrder`, `markServed`, `markPaid`, `leave`, `assign`, `release` ...) are package-private,
  so only the domain classes that check the rules (`Waiter`, `Chef`, `Cashier`, `Restaurant`) can call them.
  The UI and the engine cannot, for example, mark a customer PAID directly.
- Each transition also checks the current status and throws if it is called out of order (e.g. `markPaid`
  requires READY_TO_PAY), so the rules "no eating before served" and "no charging before READY_TO_PAY"
  are enforced inside the objects.
- `Restaurant` returns unmodifiable lists (`getCustomers`, `getTables`, `getWaitingQueue`).
- Money is kept as integer cents; revenue only changes inside `Restaurant.recordPayment`.

**Inheritance and abstraction**
- `Customer` (abstract) → `RegularCustomer`.
- `Staff` (abstract) → `Waiter`, `Chef`, `Cashier`, sharing the one-task-per-turn logic.

**Polymorphism**
- `Customer.loseSatisfaction()` calls the overridden `getSatisfactionLossPerTurn()`; `ConsoleUI`
  calls `getTypeName()`; `Waiter.takeOrder` calls `calculateBill(item)`. In Stage 1 only
  `RegularCustomer` exists, but the engine and `Restaurant` already work only with `Customer`.
- `GameEngine` calls `GameUI` methods without knowing the implementation: `ConsoleUI` when playing,
  `ScriptedUI` / `GreedyUI` in the checks.
- `Waiter.takeOrder` calls `FoodChoiceStrategy.choose`: `SeededRandomFoodChoice` in the game,
  `FixedFoodChoice` in the checks (e.g. Pasta for the Section 3.3 example).

**Composition**
- `Restaurant` creates and owns its `Table`s, `Waiter`, `Chef`, `Cashier` and the waiting queue, and
  holds the `Menu`.
- `GameEngine` owns the `Restaurant`, `ArrivalSchedule` and `VictoryCondition`.
- A `Customer` has an `Order`; an `Order` refers to a `MenuItem`.

## SOLID principles (only where they genuinely appear)

- **SRP:** each class has one reason to change. Display layout changes touch only `ConsoleUI`; the
  win targets only `VictoryCondition`; the arrival plan only `ArrivalSchedule`; the turn order only
  `GameEngine`; a Waiter rule only `Waiter`.
- **OCP:** a new customer type with a different satisfaction loss or bill is a new `Customer`
  subclass. `GameEngine`, `Restaurant` and the staff do not change. A new interface (e.g. a GUI)
  or food-choice rule is a new implementation of `GameUI` / `FoodChoiceStrategy`. (Honest limit:
  `ArrivalSchedule` must still be edited to create the new customer types. This is a small setup change.)
- **LSP:** any `Customer` subclass can be used wherever a `Customer` is expected because the
  service flow is fixed in the base class and subclasses only supply values. `ScriptedUI` replaces
  `ConsoleUI` without changing engine behaviour, which is how the checks replay games.
- **DIP:** `GameEngine` depends on the `GameUI` and `FoodChoiceStrategy` abstractions, not on
  `ConsoleUI` or `java.util.Random`. `Main` (or a test) chooses the concrete classes.
- **ISP:** not claimed. `FoodChoiceStrategy` is a one-method interface, but `GameUI` is a single
  interface, and test UIs implement some display methods as empty bodies. We kept one `GameUI`
  because the engine needs all of it and splitting it would add types without a real client
  that needs only part.

## Decisions where the brief left a choice

- **Seating:** the Waiter may seat any queued customer (not only the first); they go to the
  lowest-numbered free table.
- **Food choice:** the customer chooses when the order is taken, using `java.util.Random` with the
  game seed (default 2002, `--seed N` to change). The choice is printed immediately.
- **Leaving with a READY order:** if a customer reaches 0 while their order is READY but not served,
  the order is also cancelled; it can no longer be served and never counts as revenue.
- **"Oldest first" for the Cashier:** the customer who became READY_TO_PAY earliest pays first;
  ties go to the earlier arrival.
- **Typing errors** (unknown command, bad id) are handled by the UI; **rule errors** (no free
  table, dish not READY, ...) come from the domain as an `ActionResult` reason. Neither uses up the task.

## Prepared for later stages (no Stage 2/3 behaviour implemented)

- `Customer` hooks (`getSatisfactionLossPerTurn`, `calculateBill`, `getTypeName`) for VIP / Critic.
- `Order` locks its price when the order is taken (needed for discounts that start or end later).
- `MenuItem` is a class rather than an enum, so a combo can be modelled as a menu item.
- `Staff` base class for a new staff role.

## Trade-offs and an alternative considered

- **Enum + switch for customer types (rejected):** a `CustomerType` enum with a `switch` in the
  engine for satisfaction loss would be shorter now, but every new type would mean editing the
  engine. Stage 2 requires behaviour to be dispatched through the customer objects.
- **Rules inside staff classes vs. one big rules class:** we put each staff member's checks in that
  staff class (`Waiter`, `Chef`, `Cashier`). This keeps each class small and easy to explain, at the
  cost of the staff needing a reference to the `Restaurant` for some checks.
- **Validate-then-execute immediately:** the brief lists "choices" then "execution" as separate steps.
  We validate and execute each staff choice as soon as it is made (Waiter first, then Chef). The
  outcome is the same because the Waiter always acts before the Chef, and it lets an invalid
  choice be rejected and re-asked straight away.
