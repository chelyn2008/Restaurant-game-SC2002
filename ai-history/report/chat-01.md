# SC2002 AI Conversation — Report Preparation

- Member: Chelyn
- AI tool: ChatGPT - Project: 2002
- Date: 11 November 2026
- Purpose: Understanding the project code and preparing the report

## Exchange 1

### User
Attached:
- [Project briefing](../files/SC2002_Group_Project_Briefing.pdf)
- [Assignment specification](../files/SC2002_Restaurant_Rush_Group_Assignment.docx)
- [Project code](../files/Restaurant-game-SC2002.zip)

help me find out what i need to put in my report

### ChatGPT
Your report needs to explain **your game’s design, how it evolved across the three stages, evidence that it works, and each member’s individual reflection**.

I checked both official documents. The assignment’s Sections 5–6 specify the report content, and briefing slides 5 and 9–10 add clarification about the diagrams and reflections.

:chatgpt-content-reference{index="0"}

:chatgpt-content-reference{index="1"}

Use this report structure. The organisation is my suggestion; the required content comes from those documents.

| Section | What to include |
|---|---|
| **1. Introduction and game overview** | Briefly explain Restaurant Rush, the manager’s decisions, and the victory conditions: at least **4 paid customers, $45 revenue and 60 average paid-customer satisfaction after 18 turns**. Summarise what each stage adds. |
| **2. Architecture and class responsibilities** | Explain your boundary, control and entity layers. Include the required **table of key abstractions and responsibilities**, covering classes such as `GameEngine`, `Restaurant`, `Customer`, `Order`, staff, `GameUI` and `PricingPolicy`. |
| **3. Class diagrams for Stages 1, 2 and 3** | Include **all three diagrams**, each matching its stage’s code. Show key attributes and operations with visibility, inheritance, interfaces, associations and relevant multiplicities. Annotate meaningful examples of encapsulation, polymorphism and composition. Explain the main changes beneath each diagram. |
| **4. Design evolution and GitHub evidence** | For **Stage 1 → 2** and **Stage 2 → 3**, show new classes/files, existing classes changed or deleted, and **why**. Explain the effect on the design and which earlier behaviour remains correct. Include stage tags and commit/compare links. File counts alone are insufficient. |
| **5. Sequence diagram and explanation** | Include **one complete Stage 2 or Stage 3 scenario**. State its starting conditions, manager decision and outcome. Show arrival, ordering, staff assignments, READY → SERVED, eating, payment and the final result check, including a Stage 2/3 behaviour. Explain relevant guards, alternatives and loops. |
| **6. OO principles and design decisions** | Explain encapsulation, inheritance, polymorphism and composition using your classes. Discuss SOLID principles **only where supported by the implementation**, trade-offs, and **one alternative design considered**. Explain why the selected design was suitable. |
| **7. Functional evidence and results** | Include reproducible victory and defeat evidence, test settings/seeds, significant commands or decisions, expected versus actual results, and selected screenshots/log extracts. Cover stage-specific features, invalid input, timing rules and checks that earlier behaviour still works. |
| **8. Individual reflections** | One separately named section per member: **own contribution, specific learning, application of course concepts, hardest challenge, attempts to resolve it, and what was learned**. Use concrete examples from that person’s work. |

**You already have much of the supporting material in your ZIP:**

| Existing material | Where to use it |
|---|---|
| `DESIGN-NOTES.md` | Architecture, responsibilities, OO/SOLID discussion, trade-offs and alternative design |
| `CHANGES-stage1-to-2.md` and `CHANGES-stage2-to-3.md` | Design evolution and regression evidence |
| `ClassDiagram/` | The three stage class diagrams |
| `SequenceDiagram/` | Your Stage 3 sequence diagram |
| `TESTING.md` and `runs/` | Test cases, reproducible runs and outcome evidence |
| `README.md` | Stage tags, comparison links and execution instructions |

These are useful source materials for writing the report; **the report should still contain the diagrams, explanations and selected results itself**.

:chatgpt-content-reference{index="2"}

For your project, particularly useful discussion examples are:

- **Polymorphism:** Regular, VIP and Critic customers supply different satisfaction loss, pricing and food-choice behaviour.
- **Extensibility:** the Chef handles a `ComboMeal` through the existing preparation-unit mechanism.
- **Encapsulation:** `HappyHour` owns its activation/duration rules; an order stores its locked price.
- **Meaningful player choice:** your documented Stage 3 comparison changes Happy Hour activation, producing **64.5 average satisfaction and Victory**, versus **53.8 and Defeat** without activation.
- **Alternative design:** discuss the documented alternative of putting Happy Hour flags and discount checks across the engine/UI, and why separate `HappyHour` and `PricingPolicy` classes were selected.

**Two things to watch:** the briefing explicitly requires three class diagrams, even though the assignment table mentions the final diagram; and your design notes explicitly **do not claim ISP**, so don’t force an example just to cover all five SOLID principles.

Neither supplied document specifies a report page limit or a mandatory report template. The final report must be submitted as a **PDF**.

## Exchange 2

### User
• table of key abstractions and their responsibilities
• Final diagrams
• design discussion ( OOP concepts/ SOLID design principle) 
• functional evidence 
• Trade-offs and one alternative design
• How player choices change the result
• reflections 

are all these needed? because I was abit confused about the key abstraction part

### ChatGPT
Yes—**all seven items are required**, although they can be grouped under broader sections. For diagrams, include **three class diagrams and one sequence diagram**, following the briefing.

The **key abstractions table** simply means: **identify the important classes/interfaces in your design and explain what each is responsible for**. “Abstraction” here doesn’t mean only Java `abstract` classes.

For example, for your game:

| Key abstraction | Responsibility |
|---|---|
| `Customer` | Represents a customer’s satisfaction and service status, with overridable behaviour for different customer types. |
| `Order` | Stores the selected dish, locked bill price, preparation progress and order status. |
| `Restaurant` | Manages shared restaurant state, including tables, the waiting queue and revenue. |
| `Staff` | Defines the shared rule that each staff member can perform at most one task per turn. |
| `GameEngine` | Coordinates the turn sequence and processes manager choices. |
| `GameUI` | Defines the input and output operations the engine needs from a user interface. |
| `PricingPolicy` | Calculates the order price by applying customer discounts before active promotions. |
| `HappyHour` | Manages promotion activation, duration and discount behaviour. |

You can add other important classes, such as `Waiter`, `Chef`, `Cashier`, `Host` and `ComboMeal`. **You don’t need a row for every small helper class.**

The distinction is:

- **Responsibilities table:** what each important class/interface does.
- **Design discussion:** why you assigned those responsibilities that way, and how this demonstrates OOP/SOLID.
- **Class diagrams:** how those classes are structured and related.

The assignment explicitly asks for the responsibilities table, trade-offs, one alternative design and how player choices affect the result in Section 5.

:chatgpt-content-reference{index="0"}

## Exchange 3

### User
then what is the final structure of the report

### ChatGPT
Use this structure for your **Restaurant Rush report**:

| Section | What to include |
|---|---|
| **Cover page** | Project title, module, lab group, assignment group and members’ names/matriculation numbers. |
| **Table of contents** | Sections and page numbers. |
| **1. Introduction** | Brief game overview, objective, victory conditions and what each stage introduces. |
| **2. System Architecture and Key Abstractions** | Explain the boundary/control/entity organisation. Include the **table of important classes/interfaces and their responsibilities**. |
| **3. Class Diagrams and Design Evolution** | **3.1 Stage 1:** diagram and baseline design. **3.2 Stage 2:** diagram, additions/modifications and reasons. **3.3 Stage 3:** diagram, additions/modifications and reasons. Include GitHub comparison links and evidence that earlier behaviour remains correct. |
| **4. Sequence Diagram** | State the selected Stage 2/3 scenario, starting conditions and outcome. Include the diagram and explain the main interactions, decisions and loops. |
| **5. Design Discussion** | **5.1 OOP concepts:** encapsulation, abstraction, inheritance, polymorphism and composition, using project examples. **5.2 SOLID:** principles genuinely demonstrated. **5.3 Trade-offs and alternative design:** explain trade-offs and one alternative considered, with reasons for your chosen approach. |
| **6. Functional Evidence and Player Choices** | **6.1 Testing:** test cases, expected/actual results and selected screenshots/logs covering all stages. **6.2 Reproducible runs:** victory/defeat results and settings. **6.3 Player choices:** compare decisions and explain how they change the result. |
| **7. Individual Reflections** | A separately named subsection for each member: contribution, learning, application of course concepts, challenges and solutions. |
| **References / Appendices, if needed** | Sources used, additional test details or lengthy supporting evidence. |

**The cover page and contents page are recommended formatting choices.** The main assessed content is in Sections 2–7, with Section 1 giving the reader context.

You can keep **“How player choices change the result” inside functional evidence**, and **trade-offs inside design discussion**—they do not need separate chapters.

## Exchange 4

### User
should this 2 be under the same main header

### ChatGPT
Yes, you can group both under **“3. UML Diagrams and Design Evolution”**:

- **3.1 Class Diagrams and Stage Evolution**
  - **3.1.1 Stage 1:** baseline diagram and explanation
  - **3.1.2 Stage 2:** diagram, changes from Stage 1 and reasons
  - **3.1.3 Stage 3:** diagram, changes from Stage 2 and reasons
- **3.2 Sequence Diagram**
  - Scenario, starting conditions and outcome
  - Diagram and explanation of interactions, decisions and loops

Put the GitHub comparisons and checks of earlier behaviour alongside the relevant stage transition. **The requirements specify the content, so you can organise the headings this way.**

## Exchange 5

### User
Attached:
- [Screenshot](../files/image(20261111-081020).png)

the ones in bold are my current headers, do you think theyre okay

### ChatGPT
Yes, the bold headers are mostly suitable! I’d make **three adjustments**:

- **Design Considerations** should be a main heading at the same level as **UML Diagrams and Design Evolution**. Put OOP concepts and SOLID here.
- Rename **Trade-offs and Limitation** to **Trade-offs, Limitations and Alternative Design**, and place it under Design Considerations.
- Add **Functional Evidence and Player Choices** as a main heading—you’re currently missing this section.

Your hierarchy would then be:

1. **Introduction**
2. **System Architecture and Key Abstractions**
3. **UML Diagrams and Design Evolution**
   - Class Diagrams
     - Stage 1
     - Stage 2
     - Stage 3
   - Sequence Diagram
4. **Design Considerations**
   - OOP Concepts
   - SOLID Principles
   - Trade-offs, Limitations and Alternative Design
   - Extra Feature: Leave Warnings and Hints
5. **Functional Evidence and Player Choices**
6. **Individual Reflections**

Combine your two existing extra-feature headings into **“Extra Feature: Leave Warnings and Hints”**, and remove the editing reminder before submission.

## Exchange 6

### User
should i number the headings

### ChatGPT
Yes, **numbering will make the report easier to navigate**, especially with three stages and several subsections.

Use this format:

**1. Introduction**  
**2. System Architecture and Key Abstractions**  
**3. UML Diagrams and Design Evolution**  
　3.1 Class Diagrams  
　　3.1.1 Stage 1  
　　3.1.2 Stage 2  
　　3.1.3 Stage 3  
　3.2 Sequence Diagram  
**4. Design Considerations**  
　4.1 OOP Concepts  
　4.2 SOLID Principles  
　4.3 Trade-offs, Limitations and Alternative Design  
　4.4 Extra Feature: Leave Warnings and Hints  
**5. Functional Evidence and Player Choices**  
**6. Individual Reflections**

In Word, use **Heading 1, Heading 2 and Heading 3**, then apply a **multilevel list linked to those styles** so the numbering and table of contents update automatically.

Leave the cover page and “Table of Contents” heading unnumbered.

## Exchange 7

### User
_Selected: 4.3 Trade-offs, Limitations and Alternative Design
　4.4 Extra Feature: Leave Warnings and Hints_
 
what is the difference between these 2

### ChatGPT
They serve different purposes:

| Section | What you explain | Example from your project |
|---|---|---|
| **4.3 Trade-offs, Limitations and Alternative Design** | The benefits and costs of your design decisions, its limitations, and another approach you considered. | Separating `HappyHour` and `PricingPolicy` adds classes but keeps promotion timing and pricing responsibilities clear. An alternative was storing Happy Hour flags directly in `GameEngine`. |
| **4.4 Extra Feature: Leave Warnings and Hints** | What your additional feature does, how it works and how it fits into your design. | Explain the leaving-turn warnings, the `hint` command, and how `ServiceAdvisor` provides advice without using a staff task. |

**4.3 evaluates your design choices; 4.4 describes your extra feature.**

They can overlap slightly. For example, the warning’s limitation—it ignores future Happy Hour recovery—can be explained in **4.4**, without repeating it in 4.3.

## Exchange 8

### User
but what if we didnt have an alternative approach, should i just come up with one?

### ChatGPT
Yes—you can **consider an alternative now**, but don’t claim your group discussed or tried it earlier if that didn’t happen. You don’t need to implement it.

For example, compare your current design against **storing Happy Hour’s activation and duration directly in `GameEngine`, with discount calculation inside `Waiter`**:

- **Benefit:** fewer classes and simpler initial setup.
- **Drawback:** promotion timing and pricing responsibilities become mixed into turn coordination and order handling.
- **Why retain your design:** `HappyHour` manages promotion state, while `PricingPolicy` manages discount calculation.

Present it honestly as **“An alternative to the implemented design would be…”**

Your existing `DESIGN-NOTES.md` already describes this alternative, so check it with your teammates before using wording that implies it was considered during development.

## Exchange 9

### User
should my introduction just be a short overview

### ChatGPT
Yes—**one or two short paragraphs is enough**. Include:

- **What the game is:** a Java command-line, turn-based restaurant management game.
- **The player’s role:** allocating limited staff tasks and prioritising customers/orders.
- **The objective:** within 18 turns, achieve at least 4 paid customers, $45 revenue and 60 average satisfaction among paid customers.
- **Development scope:** three stages, progressing from basic operations to customer types, then Host, ComboMeal and Happy Hour.

Keep detailed rules, class explanations and design decisions in the later sections.

## Exchange 10

### User
how should i start?

### ChatGPT
Start by introducing **the game and the player’s role**. You could write:

Restaurant Rush is a Java command-line, turn-based restaurant management game in which the player takes on the role of a restaurant manager. With limited staff capacity, the player must prioritise seating customers, taking orders, preparing food and serving dishes to balance revenue and customer satisfaction.

Then add the **18-turn victory conditions**, followed by a brief sentence explaining the **three development stages**.

## Exchange 11

### User
okay, explain what my entire code does so that i can write my report better

### ChatGPT
Your code implements **a restaurant simulation where the player allocates limited staff actions, while the objects enforce the rules**. The main challenge is serving enough customers quickly enough to earn revenue while keeping satisfaction high.

I’ve read the Java source in your ZIP. Below is a walkthrough of how it fits together, with examples you can use to understand your report.

**1. The overall organisation**

Your project separates the program into these parts:

| Part | Main classes | Purpose |
|---|---|---|
| Entry point | `Main` | Reads launch options and creates the game. |
| Control | `GameEngine`, `GameConfig`, `ArrivalSchedule`, command classes, `VictoryCondition` | Coordinates turns, settings, arrivals, player commands and the final result. |
| Domain | `Restaurant`, `Customer`, `Order`, `Table`, staff and pricing classes | Stores game state and implements restaurant rules. |
| Menu domain | `Menu`, `MenuItem`, `ComboMeal` | Represents available food, prices and preparation requirements. |
| Boundary/UI | `GameUI`, `ConsoleUI` | Collects player input and displays information. |

The distinction between `GameEngine` and `Restaurant` is especially useful:

- **`GameEngine` decides when things happen:** arrivals first, staff actions next, payment later.
- **`Restaurant` manages the restaurant’s state and shared rules:** who occupies tables, revenue, satisfaction penalties and departures.
- **Staff and other domain objects carry out their own responsibilities:** the Waiter validates serving, the Chef advances preparation, and the Cashier chooses an eligible payment.

**2. How the game starts**

Java begins in `Main.main()`.

`Main` reads these optional arguments:

| Option | Effect |
|---|---|
| `--stage 1`, `2` or `3` | Selects the stage; the default is Stage 3. |
| `--seed N` | Sets the random seed; the default is `2002`. |
| `--echo` | Prints input commands into the output, useful for replay logs. |
| `--hints` | Enables departure warnings and the hint command. |

It then creates:

```java
GameConfig config = GameConfig.forStage(stage, seed);
ConsoleUI ui = new ConsoleUI(System.in, System.out, echo);
new GameEngine(config, ui, new SeededRandomFoodChoice(seed)).run();
```

In plain language: **choose the settings, create the terminal interface, create the food-selection mechanism, then run the game**.

`Main` connects the objects; it does not implement restaurant rules.

**3. How stages are configured**

`GameConfig` is a Java `record` that holds the settings for one game.

All stages use:

- 18 turns and 2 tables.
- Arrivals on turns 1, 4, 7, 10, 13 and 16.
- Victory targets of 4 paid customers, $45 revenue and 60 average paid-customer satisfaction.

The selected stage changes these features:

| Feature | Stage 1 | Stage 2 | Stage 3 |
|---|---|---|---|
| Customers | Regular | Regular, Critic and VIP | Same mixed schedule |
| Menu | Three base dishes | Three base dishes | Base dishes plus three combos |
| Host | Absent | Absent | Present |
| Happy Hour | Absent | Absent | Present |

The final code uses configuration switches to support all three modes. **The historical Git tags are still needed to show what the actual source looked like at each development stage.**

**4. How customers arrive**

`ArrivalSchedule` stores a list of `Arrival` records. Each contains:

- The arrival turn.
- A `CustomerFactory` that creates the required customer type.

For Stages 2 and 3:

| Customer | Turn | Type |
|---|---:|---|
| C1 | 1 | Regular |
| C2 | 4 | Critic |
| C3 | 7 | VIP |
| C4 | 10 | Regular |
| C5 | 13 | Regular |
| C6 | 16 | Regular |

An entry such as:

```java
new Arrival(4, CriticCustomer::new)
```

means: **on turn 4, use the Critic constructor to create that customer**.

The engine asks the schedule whether anyone arrives this turn. If someone does, `Restaurant.admitCustomer()` adds them to both the customer records and waiting queue.

This keeps customer creation out of the turn logic.

**5. What happens during each turn**

`GameEngine.run()` loops through all 18 turns. It does not stop early when the victory targets are reached.

Each call to `playTurn()` follows this order:

| Step | What the code does |
|---|---|
| 1 | Resets every staff member’s task availability. |
| 2 | Creates and admits the scheduled arrival. |
| 3 | Offers Happy Hour if present and unused, then applies recovery if active. |
| 4 | Displays the current state, available tasks and optional warnings. |
| 5 | Processes the Host’s action, if present. |
| 6 | Processes the Waiter’s action. |
| 7 | Processes the Chef’s action. |
| 8 | Moves customers served on an earlier turn to `READY_TO_PAY`. |
| 9 | Automatically collects at most one eligible payment. |
| 10 | Applies waiting penalties and removes customers whose satisfaction reaches zero. |
| 11 | Displays the end-of-turn results. |

After the Host or Waiter acts, the engine refreshes the next staff member’s available tasks. That produces messages such as:

```text
Chef can now: cook C1 | wait
```

For example, an order taken by the Waiter becomes available for the Chef **within the same turn**.

**6. What `Restaurant` stores**

`Restaurant` holds the shared game state:

- Menu and tables.
- Waiting queue.
- All customers who have arrived, including those who later pay or leave.
- Waiter, Chef and Cashier.
- Optional Host and Happy Hour.
- Pricing policy.
- Revenue in cents.

Keeping all customer records allows it to calculate paid counts, unhappy departures and the final average.

It also provides shared operations such as:

| Method | Purpose |
|---|---|
| `findFreeTable()` | Finds the lowest-numbered free table. |
| `whyCannotSeat()` | Checks seating eligibility for both Host and Waiter. |
| `seatCustomer()` | Removes a customer from the queue and associates them with a table. |
| `finishEating()` | Identifies customers who have completed eating. |
| `applyWaitingPenalties()` | Reduces satisfaction and handles abandonment. |
| `recordPayment()` | Adds revenue, marks payment and releases the table. |

**Why share the seating check?** Both Host and Waiter can seat customers. They call the same rule instead of maintaining separate copies.

**7. Customers and their different behaviours**

`Customer` is an abstract class containing the information common to every customer:

- ID and arrival turn.
- Satisfaction and status.
- Table and order references.
- Turn served and turn ready to pay.
- Satisfaction recorded at payment.

Its subclasses supply the differences:

| Type | Satisfaction loss per waiting turn | Bill | Dish choice |
|---|---:|---|---|
| `RegularCustomer` | 8 | Full price | Seeded random selection |
| `VIPCustomer` | 5 | 10% discount | Seeded random selection |
| `CriticCustomer` | 12 | Full price | Most expensive menu item |

For example, the restaurant calls:

```java
customer.getSatisfactionLossPerTurn()
```

The object supplies 8, 5 or 12 depending on its actual class.

**This is polymorphism:** the calling code works through `Customer` without a chain of checks for Regular, VIP and Critic.

The same approach applies to:

- `calculateBill()` — VIP overrides the standard bill.
- `chooseDish()` — Critic overrides the standard food-selection behaviour.

The Critic chooses Pasta in the base menu and Burger + Pasta Combo in the Stage 3 menu because those are the most expensive items.

**8. Customer status and order status are separate**

Your code tracks two related processes.

| Customer status | Meaning |
|---|---|
| `WAITING` | In the queue |
| `SEATED` | At a table without an order |
| `ORDERED` | Order placed, still awaiting service |
| `SERVED` | Eating |
| `READY_TO_PAY` | Finished eating |
| `PAID` | Paid and left |
| `LEFT` | Abandoned without paying |

| Order status | Meaning |
|---|---|
| `PLACED` | Ordered, no preparation completed |
| `IN_PREPARATION` | Some preparation completed |
| `READY` | Finished cooking |
| `SERVED` | Delivered |
| `PAID` | Payment completed |
| `CANCELLED` | Customer abandoned before service |

**A customer remains `ORDERED` even when their dish is `READY`.** The food is cooked, but they have not received it yet, so they still lose satisfaction.

Methods check the expected status before changing it. For example, `Customer.placeOrder()` requires the customer to be `SEATED`. This helps prevent invalid transitions.

**9. How the staff work**

`Staff` is an abstract superclass with a private `taskDone` flag.

It provides the shared behaviour:

- `isAvailable()` checks whether the staff member has acted.
- `completeTask()` consumes their action.
- `resetForNewTurn()` makes them available again.
- `waitThisTurn()` deliberately uses their turn without doing work.

The subclasses perform different jobs:

| Class | Responsibility |
|---|---|
| `Host` | Seats one queued customer. |
| `Waiter` | Seats, takes an order, or serves one ready dish. |
| `Chef` | Advances one order by one preparation unit. |
| `Cashier` | Automatically collects one eligible payment. |

The Host’s task is **additional** to the Waiter’s. In Stage 3, the Host can seat C1, the Waiter can take C1’s order, and the Chef can begin preparing it in the same turn.

The Waiter still cannot both take an order and serve another dish that turn.

**10. How taking an order works**

When the player types `order C1`:

1. `ConsoleUI` reads the text.
2. `WaiterCommand.parse()` converts it into a command object.
3. `GameEngine` finds C1 and calls `Waiter.takeOrder()`.
4. The Waiter checks that C1 is seated and that its own task is available.
5. C1 chooses a dish through `chooseDish()`.
6. `PricingPolicy` calculates the bill.
7. A new `Order` stores the dish, bill and order turn.
8. C1 becomes `ORDERED`.
9. The Waiter’s task is consumed.

**The player chooses whom to take an order from, but never chooses that customer’s dish.**

`FoodChoiceStrategy` defines the selection operation. `SeededRandomFoodChoice` implements it using a seeded `Random`.

The same seed and same sequence of decisions reproduce the same choices. Changing the order in which customers order can change which random choice a customer receives.

**11. How preparation, service and payment are delayed**

`Order` stores `unitsDone` and compares it with the dish’s required preparation units.

Each Chef action calls:

```java
order.prepareOneUnit(turn);
```

When enough units are completed, the order becomes `READY` and records `readyOnTurn`.

The Waiter checks that the order became ready **on an earlier turn** before serving it.

Similarly:

- A customer served this turn cannot finish eating until a later turn.
- A customer becoming ready to pay this turn cannot pay until a later turn.

For a Regular customer ordering Pasta with immediate attention:

| Turn | Action | Satisfaction at turn end |
|---:|---|---:|
| 1 | Waiter seats customer | 92 |
| 2 | Waiter takes order; Chef completes unit 1 | 84 |
| 3 | Chef completes unit 2; order becomes READY | 76 |
| 4 | Waiter serves | 76 |
| 5 | Customer becomes READY_TO_PAY | 76 |
| 6 | Cashier collects payment and frees table | 76 recorded |

There is no separate `EATING` enum value: **`SERVED` represents the eating period**.

The Cashier chooses the customer who became ready to pay earliest; arrival time breaks a tie.

**12. How satisfaction, abandonment and revenue work**

At turn end, customers in `WAITING`, `SEATED` or `ORDERED` lose satisfaction.

Customers who have been served stop losing satisfaction.

If satisfaction reaches zero:

1. The customer is removed from the queue, if present.
2. Their table is released, if occupied.
3. Their unserved order is cancelled, if one exists.
4. Their status becomes `LEFT`.
5. No revenue is recorded.

At payment:

1. The order’s locked bill is added to revenue.
2. Satisfaction is recorded.
3. Customer and order become `PAID`.
4. The table is released.

**Ordering and serving do not generate revenue; payment does.**

The average uses only the satisfaction recorded for **paid customers**. When nobody has paid, the program displays `N/A`.

**13. Menu items and combos**

`MenuItem` stores a dish’s name, price in cents and preparation units.

| Base dish | Price | Units |
|---|---:|---:|
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |

`Menu.standard()` creates those three dishes.

`Menu.withCombos()` adds:

| Combo | Price | Units |
|---|---:|---:|
| Salad + Burger | $21.60 | 2 |
| Salad + Pasta | $24.30 | 3 |
| Burger + Pasta | $29.70 | 3 |

`ComboMeal extends MenuItem` and retains references to its two components.

Its price is 90% of the combined component prices, and its preparation units are their sum.

**Why this design helps:** the Chef already understands preparation units, so it handles combos through the existing `MenuItem` operations. It does not need a special “cook combo” branch.

The component dishes are shared menu objects, so containing references to them does not by itself establish strict UML composition.

**14. Happy Hour and pricing**

Happy Hour is split across three responsibilities:

| Class/interface | Responsibility |
|---|---|
| `Promotion` | Defines operations for checking activity and applying a discount. |
| `HappyHour` | Implements the promotion and owns its activation/duration state. |
| `PricingPolicy` | Applies customer pricing followed by active promotions. |

`HappyHour` records its activation turn. It is active on that turn and the next, and cannot be activated again.

During active turns:

- Waiting, seated and ordered customers recover 15 satisfaction, capped at 100.
- Newly taken orders receive 20% off.
- Already placed orders keep their original prices.

For a VIP ordering a Burger:

```text
Base price:          $15.00
After VIP discount:  $13.50
After Happy Hour:    $10.80
```

That final amount is stored in `Order.priceCents`, a `final` field.

**The bill is locked when the order is taken**, so the Cashier simply collects it later. It does not recalculate discounts.

Money uses integer cents, with rounding at each discount step. `Money.format()` converts cents into a displayed amount such as `$10.80`.

**15. Invalid input and invalid actions**

Your code handles two kinds of problems:

| Problem | Example | Handling |
|---|---|---|
| Invalid command format | `cook banana` | Parsing rejects the input; the UI explains and asks again. |
| Valid command, invalid game action | `serve C1` while food is unfinished | Domain validation returns a failed `ActionResult`; the engine explains and asks again. |

The command records—`HostCommand`, `WaiterCommand` and `ChefCommand`—store an action and optional customer ID.

`CommandParsing` handles shared text processing, such as accepting `C2`, `c2` or `2`.

`ActionResult` stores:

```java
boolean success
String message
```

Successful actions consume staff availability only after validation passes. **A failed choice therefore does not waste the staff member’s task.**

If a replay file runs out of input, the UI defaults staff actions to `wait` and Happy Hour answers to `no`, allowing the simulation to finish.

**16. Leave warnings and hints**

When `--hints` is enabled, `ServiceAdvisor` reads the game state and provides advice.

It estimates:

```text
Turn ends until leaving = ceiling(satisfaction ÷ loss per turn)
```

For a Critic with satisfaction 28:

```text
28 → 16 → 4 → 0
```

That is three turn ends, counting the current turn.

The advisor:

- Estimates departure turns.
- Selects the customer closest to leaving.
- Breaks urgency ties by earlier arrival.
- Suggests the next step: seat, order, cook or serve.

It uses `getSatisfactionLossPerTurn()`, so customer differences are handled polymorphically.

**Its limitations matter:** it ignores future Happy Hour recovery and suggests the customer’s next step rather than calculating an optimal, guaranteed-executable plan. Actual commands still undergo the normal validation.

Requesting a hint does not consume a staff task.

**17. How the final result is determined**

After turn 18, `VictoryCondition.evaluate()` checks:

```java
enoughPaid && enoughRevenue && happyEnough
```

All three must be true:

| Requirement | Target |
|---|---:|
| Paid customers | At least 4 |
| Revenue | At least $45 |
| Average satisfaction of paid customers | At least 60 |

`GameResult` stores the victory flag and explanations of which targets were met. `ConsoleUI` prints them.

Serving many people alone does not guarantee victory: they must pay before the game ends, and the average must meet the target.

**18. What the testing code does**

The `test/` folder supplies automated checks and substitute objects:

| Test class/helper | Purpose |
|---|---|
| `StageOneChecks` | Core timing, abandonment, payment order, invalid input, victory boundaries and reproducible runs. |
| `StageTwoChecks` | VIP pricing, satisfaction loss, Critic choices and polymorphic behaviour. |
| `StageThreeChecks` | Host actions, combos, Happy Hour, discount order, price locking and hints. |
| `FixedFoodChoice` | Controls food choices for predictable tests. |
| `ScriptedUI` | Supplies predetermined decisions without manual typing. |
| `GreedyUI` | Automatically chooses actions for test scenarios. |

These substitutes are possible because the engine accepts `GameUI` and `FoodChoiceStrategy` interfaces.

I couldn’t rerun the checks here because this environment has no `javac`; this explanation is based on reading the source.

**19. How this translates into your report**

| Report section | Strong examples from the code |
|---|---|
| Key abstractions | Responsibilities of `GameEngine`, `Restaurant`, `Customer`, `Order`, staff and `PricingPolicy`. |
| Encapsulation | Private state, controlled status transitions, locked order prices and unmodifiable collection views. |
| Inheritance | Customer subclasses, staff subclasses and `ComboMeal extends MenuItem`. |
| Polymorphism | Customer loss/pricing/choice methods; promotions; interchangeable UI and food-choice implementations. |
| Composition | `Restaurant` creates and holds its tables and staff. |
| SRP | UI displays, engine coordinates, pricing policy calculates discounts. |
| OCP | Combos work with the existing Chef; customer subclasses supply behaviour through shared operations. Explain the setup/controller edits honestly. |
| LSP | Different customer types follow the same service lifecycle; combos can be processed as menu items. |
| DIP | The engine receives `GameUI` and `FoodChoiceStrategy`; pricing operates through `Promotion`. |
| Trade-offs | More focused classes improve separation but require more wiring; optional features let one engine support all stages. |
| Functional evidence | Timing checks, invalid-action retries, departures, pricing and reproducible outcomes. |
| Player choices | Staff priorities and Happy Hour timing affect who pays and their recorded satisfaction. |

For writing, focus on **what each object owns, what it is allowed to change, and which other objects it asks for help**. That explains your design more clearly than listing methods one by one.

## Exchange 12

### User
should this be in the introduction or the system architecture

### ChatGPT
Put these in the **Introduction**, because they describe the game’s setup and objective.

You can summarise them in one sentence:

> Each game runs for 18 turns with two tables and six scheduled customer arrivals. Victory requires at least four paid customers, $45 in revenue and an average satisfaction of at least 60 among paid customers.

The exact arrival turns can go in your **functional evidence/test settings** if needed.

In **System Architecture**, explain how `GameConfig` stores these settings and how `GameEngine` uses them to coordinate the game.

## Exchange 13

### User 
should i have page break per numbered header

### ChatGPT
**For your main numbered headings, yes—a new page is a sensible choice.** For example, start Sections 1, 2, 3 and so on on separate pages.

For subsections such as **3.1 Class Diagrams** or **4.1 OOP Concepts**, continue on the same page unless the diagram or content needs more space.

If a main section is very short, it’s also fine to share a page to avoid excessive blank space. **This is a formatting choice, not an assignment requirement.**

In Word, use **Ctrl + Enter** for a page break instead of pressing Enter repeatedly.

## Exchange 14

### User
what is the github evidence if all we did was push to main

### ChatGPT
**GitHub evidence means showing how your code changed between stages.** Pushing everything to `main` is fine—separate development branches aren’t required.

What matters is whether the commit history contains these snapshots:

| Evidence | What it shows |
|---|---|
| `stage-1` tag | A runnable baseline with only Stage 1 features |
| `stage-2` tag | A runnable version adding VIP and Critic |
| `stage-3` tag | The final runnable version |
| Stage comparison links | Which files changed between those snapshots |
| Your explanation | Why classes were added/modified and how earlier behaviour was checked |

**Your uploaded README already lists all three stage tags, commit IDs and comparison links.** However, I haven’t verified that those tags exist on GitHub or that their code matches the descriptions.

You can check locally with:

```powershell
git tag
git log --oneline --decorate
```

If you pushed incrementally, earlier commits may already provide the required snapshots—you can tag the appropriate commits and push the tags.

**If you only committed the complete final game**, that alone won’t demonstrate the three historical stages. Also, running the final code with `--stage 1` doesn’t replace a genuine Stage 1 snapshot, because the final source still contains later-stage features.

Separately, the briefing asks each member to upload their **full AI conversation history to their own branch**, so that requirement still applies even if all development happened on `main`.

## Exchange 15

### User
Attached:
- [Screenshot](../files/image(20261111-091028).png)

wdym within our own branch? should i put this chat in

### ChatGPT
**Yes—include this chat**, because it helped you understand the code and prepare the project report. Include your other project-related AI chats too, with **your prompts, AI replies and follow-ups in order**, rather than just a summary.

**“Your own branch” means a named branch within the same group repository**, for example:

| Branch | Contents |
|---|---|
| `main` | Your group’s project code and submission materials |
| `chelyn-ai-history` | Your AI conversation records |
| Other members’ personal branches | Their respective AI conversation records |

You can create yours through GitHub:

1. Open your group repository.
2. Click the branch dropdown currently showing **`main`**.
3. Type **`chelyn-ai-history`** and select **Create branch**.
4. On that branch, create a file such as `ai-history/report/chat-01.md`.
5. Paste this conversation into it, clearly labelling **User** and **Assistant**, then commit it to that branch.
6. Add a link to your AI-history folder in the **README on `main`**.

The slide’s stage folders are examples. A **`report` folder** clearly describes this conversation; use stage folders for chats about particular stages.

You can create this branch now even though your coding was done on `main`—it records your AI use and doesn’t imply you developed the code separately.

## Exchange 16

### User
how should i format my markdown

### ChatGPT
Use a simple format with a title, context, and **numbered exchanges in chronological order**. You can copy this into your `.md` file:

```markdown
# SC2002 AI Conversation — Report Preparation

- Member: Chelyn
- AI tool: ChatGPT
- Date: 11 November 2026
- Purpose: Understanding the project code and preparing the report

## Exchange 1

### User
help me find out what i need to put in my report

### ChatGPT
[Paste the full response here.]

## Exchange 2

### User
[Paste your next message here.]

### ChatGPT
[Paste the full response here.]
```

**Keep the complete wording**, including follow-up questions and corrections. Preserve tables, lists and code blocks from the responses.

For messages involving an attachment, note its filename. Save relevant screenshots alongside the Markdown file and embed them like this:

```markdown
### User
the ones in bold are my current headers, do you think theyre okay

![Report headings](images/report-headings.png)
```

A suitable filename is **`ai-history/report/chat-01.md`**. The exchange numbering is just for readability; the important requirement is the full conversation in order.
