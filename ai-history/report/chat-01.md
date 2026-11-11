# SC2002 AI Conversation — Report Preparation

- Member: Chelyn
- AI tool: ChatGPT - Project: 2002
- Date: 11 November 2026
- Purpose: Understanding the project code and preparing the report

## Exchange 1

### User
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
[Paste your next message here.]

### ChatGPT
[Paste the full response here.]
