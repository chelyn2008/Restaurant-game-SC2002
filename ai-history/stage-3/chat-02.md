# AI chat history: program structure and design guidance (Stage 3)
By Goh Cai Ying

Exported from a Copilot SDK in VS Code conversation. Prompts and replies are included in order.
The attached PDFs and source file are named where relevant; tool output is omitted.

---

## Prompt 1 — 2026-11-14 10:21 SGT

*Attached: `SC2002_Group_Project_Briefing.pdf`, `SC2002_Restaurant_Rush_Group_Assignment (1).pdf`, `README.md`. The user requested that guidance refer only to the two PDFs.*

this is my group assignment, please guide me thru where shld i start and how shld i draft the structure of the program by just referring to the 2 pdf file above as im in charge of the coding part

**AI assistant:**

## Where to start

Start with **Stage 1 only**. The project briefing says to treat later requirements as if they arrive in stages; the assignment brief also expects three runnable versions that build on one another. So keep Stage 1 free of VIPs, Critics, the Host, ComboMeals, and Happy Hour. Save a runnable version before extending it.

The base game is precisely specified: 18 turns, 2 tables, one Waiter, one Chef, one Cashier, and six scheduled arrivals. Victory requires at least 4 paid customers, $45 revenue, and an average paid-customer satisfaction of 60 or higher. These are good requirements to turn into a checklist before writing code. See the [Restaurant Rush assignment PDF](</C:/Users/raingoh/Downloads/SC2002_Restaurant_Rush_Group_Assignment%20(1).pdf>) and the [group project briefing PDF](</C:/Users/raingoh/Downloads/SC2002_Group_Project_Briefing.pdf>).

## A reasonable starting structure

Keep the command-line interface separate from game rules. For example:

```text
src/main/java/restaurant/
  Main.java
  ui/
    ConsoleUI.java
  control/
    GameController.java
    TurnController.java
  domain/
    Game.java
    Customer.java
    RegularCustomer.java
    Order.java
    MenuItem.java
    Table.java
    OrderStatus.java
    CustomerStatus.java
  staff/
    Waiter.java
    Chef.java
    Cashier.java
```

Treat this as a starting point, not a required class list. Keep it simple: the `ConsoleUI` reads choices and displays information; the controllers coordinate the game; domain objects hold state and game behavior. The assignment explicitly says not to put rules in the UI.

## Build it in this order

1. **Write down the turn sequence first.** Implement arrivals, manager choices, kitchen/serving, eating/payment, satisfaction/abandonment, and the result check in that order.
2. **Implement Stage 1's basic domain objects and flow.** Get one customer from arrival through payment before building the full 18-turn game.
3. **Add input validation and a complete turn display.** Invalid choices must explain the problem and let the manager try again without using up the staff task.
4. **Test both outcomes.** Create reproducible Stage 1 victory and defeat runs, as required.
5. **Save and tag the runnable Stage 1 version**, then extend it for Stage 2 and Stage 3. Keep notes on which files each stage adds or changes and why.

## Watch these timing rules closely

These are easy places to accidentally change the assignment's behavior:

- The Waiter acts before the Chef. The Chef may work on an order taken that turn, but the Waiter **cannot serve a dish that becomes READY that turn**.
- A customer served this turn must eat for a full turn before becoming ready to pay; payment happens no earlier than the following turn.
- A customer who becomes ready to pay this turn cannot pay until the next turn.
- A table remains occupied until payment or departure.
- Revenue includes paid bills only. An abandoned customer's unfinished order must be cancelled.
- The game runs all 18 turns; it does not end early.

A useful implementation technique is to record the turn when an order becomes ready or a customer is served/ready to pay. That makes it easier to enforce the "not until next turn" rules.

## Plan the later extensions without building them yet

For **Stage 2**, add `VIPCustomer` and `CriticCustomer` as customer objects with their own behavior—such as satisfaction loss and discounts. The brief specifically says not to implement their differences with a chain of customer-type checks in the turn controller.

For **Stage 3**, add the Host as an extra seating role, model `ComboMeal` as a combination of two existing menu items, and keep the Happy Hour price locked into the order when it is placed. Store money in integer cents, as the brief requires, and apply the VIP discount before the Happy Hour discount.

When you're ready, a good first coding task is: **implement the Stage 1 turn loop with one customer, one table, and the exact READY → SERVED → eating → payment timing**. Once that behavior is solid, expand to the full base setting.

---

## Prompt 2 — 2026-11-14 10:27 SGT

how about happy hour and also the discount for vip customer

**AI assistant:**

The assignment makes these **Stage 2 and Stage 3** features:

- **VIP discount (Stage 2):** VIPs receive **10% off** their bill. They also lose 5 satisfaction points per waiting turn, instead of a Regular customer's 8.
- **Happy Hour (Stage 3):** The manager can activate it **once per game, at the start of a turn**. It lasts for that turn and the next, and gives **20% off orders taken while it's active**. Show its remaining duration.
- **Stacking:** Apply the VIP discount first, then Happy Hour. So a VIP ordering a $15 Burger during Happy Hour pays **$15 × 0.90 × 0.80 = $10.80**.
- **Price timing:** Calculate and save the discounted price when the order is taken. Turning Happy Hour on later must not change orders already placed.
- **Satisfaction effect:** At the start of each active Happy Hour turn, customers still waiting for a table or food recover 15 satisfaction, up to 100. This doesn't apply to customers who have already been served or have left.

For your design, put VIP-specific behavior on the customer object rather than checking "is VIP?" throughout the turn controller. Keep Happy Hour's activation and remaining duration in the game state, and calculate the final bill in game logic—not in the CLI. These rules are in the [assignment PDF, Stage 2 and Stage 3 sections](</C:/Users/raingoh/Downloads/SC2002_Restaurant_Rush_Group_Assignment%20(1).pdf>).

---

## Prompt 3 — 2026-11-14 10:28 SGT

*Attached: `SeededRandomFoodChoice.java`.*

is there any suggestions on which class i shld use polymorphism / inheritance? as the guideline mention to make our code extensible and reproducible

**AI assistant:**

Yes. I’d use **Customer** as your main inheritance/polymorphism example, and your existing food-choice strategy as a separate example of interchangeable behavior. The assignment asks for VIP and Critic behavior to be handled through customer objects, not type checks in the turn controller.

### 1. Customer types: inheritance and polymorphism

A suitable design is:

```text
Customer
├── RegularCustomer
├── VIPCustomer
└── CriticCustomer
```

Keep shared state and lifecycle in `Customer`—for example, satisfaction, order, table, and status. Let each customer object supply the behavior that differs, such as:

- `satisfactionLossPerWaitingTurn()` — Regular: 8, VIP: 5, Critic: 12
- `discountRate()` — Regular: 0%, VIP: 10%, Critic: 0%
- Critic's documented order preference or service reaction

Then the turn logic can call a customer's behavior without asking whether it is a VIP or Critic. For example, Stage 2 can add `VIPCustomer` and `CriticCustomer` while leaving the common turn flow intact. You don't need to add those subclasses to the Stage 1 version.

### 2. Food choice: your existing strategy

Your [SeededRandomFoodChoice.java](</C:/Users/raingoh/Desktop/2002/Restaurant-game-SC2002/src/restaurant/domain/SeededRandomFoodChoice.java>) already implements `FoodChoiceStrategy`. That's a good use of polymorphism: the game can depend on the strategy interface and use a seeded-random implementation for repeatable choices.

For reproducible runs, use the same explicit seed, the same menu order, and the same sequence of choices. Avoid seeding with the current time. Also ensure the menu isn't empty before calling `nextInt(items.size())`. A new random choice or a changed menu order can change later outcomes for the same seed, so record the seed and setup used in your test notes.

You could give the Critic a documented preference—such as preferring Pasta—by having the **Critic object** apply that preference when choosing food. Keep the manager out of the food-selection decision.

### 3. Stage 3 ComboMeal: inheritance plus composition

A `ComboMeal` can be a `MenuItem` so the rest of the game can treat it like any other menu item. It can also **contain its two component menu items**. That is a natural example of both inheritance and composition, since the assignment defines a combo as two existing items.

You don't need to use inheritance for every staff role. Waiter, Chef, and Cashier have quite different jobs, so making them subclasses of a generic `Staff` class just to demonstrate inheritance may add little value.

For Happy Hour, I’d use game state and pricing logic rather than another inheritance hierarchy: calculate the final price when the order is placed, then save it on the order. The Stage 2 and Stage 3 behaviors are specified in the [assignment PDF](</C:/Users/raingoh/Downloads/SC2002_Restaurant_Rush_Group_Assignment%20(1).pdf>).

---

## Prompt 4 — 2026-11-14 10:30 SGT

*Attached: `SeededRandomFoodChoice.java`.*

can u draft out a rough structure of ur recommended program structure from all u mention above so i can visually how to start

**AI assistant:**

Here’s a **rough map**, not a list of files you need to create all at once. It keeps the UI separate from game rules and leaves clear places for each stage’s additions.

```text
src/restaurant/
├── Main.java
├── ui/
│   └── ConsoleUI.java               # Read choices; display game state
├── controller/
│   ├── GameController.java          # Set up and run the 18-turn game
│   └── TurnController.java          # Coordinate the steps of one turn
├── domain/
│   ├── Game.java                    # Turn, revenue, queue, tables, result
│   ├── customer/
│   │   ├── Customer.java            # Shared customer state and behavior
│   │   ├── RegularCustomer.java     # Stage 1
│   │   ├── VIPCustomer.java         # Add in Stage 2
│   │   └── CriticCustomer.java      # Add in Stage 2
│   ├── menu/
│   │   ├── Menu.java
│   │   ├── MenuItem.java
│   │   └── ComboMeal.java           # Add in Stage 3
│   ├── Order.java                   # Item, status, saved bill in cents
│   ├── Table.java
│   ├── OrderStatus.java
│   └── CustomerStatus.java
├── staff/
│   ├── Waiter.java
│   ├── Chef.java
│   ├── Cashier.java
│   └── Host.java                    # Add in Stage 3
└── pricing/
    ├── OrderPricing.java            # Calculate discounts when order is taken
    └── HappyHour.java               # Add in Stage 3
```

Your food-choice classes already fit into this design: keep `FoodChoiceStrategy` and [SeededRandomFoodChoice](</C:/Users/raingoh/Desktop/2002/Restaurant-game-SC2002/src/restaurant/domain/SeededRandomFoodChoice.java>) as the mechanism for reproducible customer choices. Your current package layout may differ; you can keep the files where they are and organize them gradually.

### How the pieces work together

```text
Main
 └─ GameController
     ├─ ConsoleUI       asks for manager decisions / displays state
     ├─ Game            owns the current game state
     └─ TurnController  runs each turn in the required order
         ├─ Waiter      seats, takes an order, or serves
         ├─ Chef        advances an order by one preparation unit
         ├─ Cashier     pays the oldest eligible customer
         └─ Customers   update satisfaction / leave if they reach 0
```

The `TurnController` should coordinate these steps, not contain every rule itself. For example, it can ask a customer for their waiting satisfaction loss rather than checking the customer's type.

### What changes at each stage

```text
Stage 1: Game + RegularCustomer + Waiter/Chef/Cashier + Menu/Order/Table
Stage 2: Add VIPCustomer and CriticCustomer
Stage 3: Add Host, ComboMeal, HappyHour, and pricing logic
```

Use `Customer` for shared behavior and subclasses for differences: VIPs have their discount and lower satisfaction loss; Critics get the preference or service reaction you document. For Stage 3, `ComboMeal` can be a `MenuItem` that contains its two component items. `OrderPricing` should calculate the final price when the order is placed and save it on `Order`, so later Happy Hour changes don’t alter existing bills.

For a first coding milestone, build **one Stage 1 customer going from arrival through payment**. Then add the full queue, two tables, turn loop, input retries, and outcome checks. Keep the 18-turn sequence and task timing in `TurnController` aligned with the assignment brief.

---

## Prompt 5 — 2026-11-14 10:34 SGT

*Attached: `SeededRandomFoodChoice.java`.*

can u help me export this chat as a markdown , as i need to attach as a supllmetary material under my branch caiying ai in github?

**AI assistant:**

I exported this conversation as a Markdown transcript at `ai-history/stage-3/chat-02.md`, added a link to it in the branch README, and pushed both changes to the `caiying-ai-history` branch on GitHub.
