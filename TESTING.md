# Testing (Stage 1)

## Automated checks

```sh
javac --release 17 -d out-test $(find src test -name "*.java")
java -cp out-test restaurant.StageOneChecks
```

Plain Java, no test library. Run from the project root. Each check prints `PASS` or `FAIL`, and the
program exits with code 1 if anything fails. Current result: **52 checks passed, 0 failed.**

The checks drive the real `GameEngine` through `ScriptedUI` (a `GameUI` that answers from a list of
commands), or call the domain classes directly when a situation cannot happen in a normal game.

| # | Requirement | Method in `StageOneChecks` | What is checked |
|---|-------------|----------------------------|-----------------|
| 1 | Section 3.3 example | `sectionThreeThreeExample` | Regular customer ordering Pasta (`FixedFoodChoice`): satisfaction at turn ends 92, 84, 76, 76, 76; READY after turn 3, SERVED on turn 4, READY_TO_PAY on turn 5, PAID on turn 6 for $18.00 with 76 recorded; table freed. |
| 2 | No serving on the READY turn | `dishCannotBeServedOnTheTurnItBecomesReady` | A Salad finished on turn 2 is refused for serving on turn 2 (reason shown, `serve C1` not offered) and succeeds on turn 3; the same in a full engine run. |
| 3 | Abandonment | `abandonmentCancelsOrderAndFreesTable` | A seated customer with an unfinished Pasta reaches 0 at the end of turn 13: status LEFT, order CANCELLED, table freed, no revenue, message shown. A queued customer (C2) also leaves the queue at 0 (turn 16). |
| 4 | Cashier oldest first | `cashierPaysOldestFirst` | Nobody pays on the turn they became READY_TO_PAY; one payment per turn; the customer READY_TO_PAY earliest pays first (ties go to the earlier arrival). |
| 5 | Invalid input keeps the task | `invalidInputDoesNotUseTheTask` | `serve C1` (no order), `dance` (not a command), `seat C9` (no such customer) are each rejected with a reason, then `seat C1` succeeds in the same turn; an invalid Chef choice is followed by a valid one; seating with no free table is refused and the Waiter stays available. |
| 6 | Win and lose boundaries | `winAndLoseBoundaries` | 4 paid / $45.00 / average 60 → Victory; 3 paid, $44.99 or average 59.75 → Defeat; nobody paid → Defeat. |
| 7 | At most five customers can pay | `atMostFiveCustomersCanPay` | With the fastest possible play (`GreedyUI`) and every customer ordering Salad, Burger or Pasta, C6 never pays; with Salad exactly 5 pay; seeds 1-50 never exceed five. |
| 8 | Runs reproduce | `savedRunsReproduce` | Replays `runs/victory.txt` and `runs/defeat.txt` with seed 2002 and compares the output with the saved logs (line endings ignored). |

## Reproducible runs

Both runs use the base setting and the **default seed 2002** (no `--seed` argument).
Each input file has one command per line, answering the prompts in order (Waiter, then Chef,
every turn; an invalid line is followed by the replacement choice).

```sh
javac --release 17 -d out $(find src -name "*.java")
java -cp out restaurant.Main --echo < runs/victory.txt > my-victory.log
diff --strip-trailing-cr my-victory.log runs/victory.log     # no output = identical
java -cp out restaurant.Main --echo < runs/defeat.txt > my-defeat.log
diff --strip-trailing-cr my-defeat.log runs/defeat.log
```

(`--echo` prints each command after its prompt so the log shows the choices.)

Customer food choices with seed 2002, in the order orders are taken:
Burger, Burger, Salad, Burger, Pasta, Salad.

### Victory run: `runs/victory.txt`, log `runs/victory.log`

Strategy: give each customer full attention. Serve before taking orders, take orders before
seating, and the Chef cooks a dish the same turn it is ordered. Turn 2 includes one deliberately
invalid choice (`serve C1` before C1 has ordered) to show it is rejected without using the task.

| Turn | Waiter | Chef | Notes |
|------|--------|------|-------|
| 1 | seat C1 | wait | C1 arrives |
| 2 | serve C1 (rejected), order C1 | cook C1 | C1: Burger, READY |
| 3 | serve C1 | wait | |
| 4 | seat C2 | wait | C2 arrives; C1 READY_TO_PAY |
| 5 | order C2 | cook C2 | C2: Burger; **C1 pays $15.00 (84)** |
| 6 | serve C2 | wait | |
| 7 | seat C3 | wait | C3 arrives |
| 8 | order C3 | cook C3 | C3: Salad; **C2 pays $15.00 (84)** |
| 9 | serve C3 | wait | |
| 10 | seat C4 | wait | C4 arrives |
| 11 | order C4 | cook C4 | C4: Burger; **C3 pays $9.00 (84)** |
| 12 | serve C4 | wait | |
| 13 | seat C5 | wait | C5 arrives |
| 14 | order C5 | cook C5 | C5: Pasta 1/2; **C4 pays $15.00 (84)** |
| 15 | wait | cook C5 | Pasta READY (cannot be served this turn) |
| 16 | serve C5 | wait | C6 arrives and waits in the queue |
| 17 | seat C6 | wait | C5 READY_TO_PAY |
| 18 | order C6 | cook C6 | C6: Salad; **C5 pays $18.00 (76)** |

**Result: VICTORY.** 5 paid (≥ 4), revenue $72.00 (≥ $45.00), average satisfaction 82.4 (≥ 60),
0 unhappy departures. C6 (arrived turn 16) cannot pay within 18 turns, as the brief expects.

### Defeat run: `runs/defeat.txt`, log `runs/defeat.log`

Strategy: an inattentive manager. The Waiter only acts every third turn (seating first) and
the Chef only cooks right after an order is taken, so customers wait a long time.

| Turn | Waiter | Chef | Notes |
|------|--------|------|-------|
| 1 | seat C1 | wait | C1 arrives |
| 2-3 | wait | wait | |
| 4 | seat C2 | wait | C2 arrives |
| 5-6 | wait | wait | |
| 7 | order C1 | cook C1 | C3 arrives; C1: Burger, READY |
| 8-9 | wait | wait | |
| 10 | serve C1 | wait | C4 arrives; C1 served at satisfaction 28 |
| 11 | wait | wait | C1 READY_TO_PAY |
| 12 | wait | wait | **C1 pays $15.00 (28)**; Table 1 freed |
| 13 | seat C3 | wait | C5 arrives |
| 14-15 | wait | wait | |
| 16 | order C2 | cook C2 | C6 arrives; C2: Burger READY, but **C2 reaches 0 and leaves**: order cancelled, table freed |
| 17-18 | wait | wait | |

**Result: DEFEAT.** 1 paid (< 4), revenue $15.00 (< $45.00), average satisfaction 28.0 (< 60),
1 unhappy departure. The two runs face the same customers and the same food choices; only the
manager's choices differ, which is why one wins and the other loses.
