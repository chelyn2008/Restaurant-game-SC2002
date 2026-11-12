# SC2002 AI Conversation — Report Preparation

- Member: Chelyn
- AI tool: ChatGPT - Project: 2002
- Date: 12 November 2026
- Purpose: Understanding the project code

## Exchange 1
### User
what is the difference between console ui and game ui

### ChatGPT
**`GameUI` defines what a user interface must do; `ConsoleUI` implements how it does those things in the terminal.**

| | `GameUI` | `ConsoleUI` |
|---|---|---|
| Type | Interface | Class implementing `GameUI` |
| Contains | Method declarations, such as `askWaiterCommand()` and `showMessage()` | Code that reads keyboard input and prints text |
| Role | Gives `GameEngine` a common contract to use | Provides the actual terminal interaction |

For example, the engine calls:

```java
ui.askWaiterCommand();
```

With **`ConsoleUI`**, this displays `Waiter>` and reads what you type. With **`ScriptedUI`** in your tests, it supplies a predetermined command.

The engine uses the same method either way—it doesn’t need to handle terminal input itself.

## Exchange 1
### User
then what does the gameui show?

### ChatGPT
**`GameUI` doesn’t display anything by itself—it’s an interface.** It declares methods for displaying information, such as:

- `showWelcome()` — introduction and settings.
- `showTurnStart()` — current restaurant state and available tasks.
- `showMessage()` — action results or errors.
- `showFinalResult()` — victory or defeat.

**`ConsoleUI` supplies the code that actually prints this information.**

So when `GameEngine` calls `ui.showMessage("Order is ready")`, the `ConsoleUI` implementation prints **“Order is ready”** in the terminal.

