# Testing (Stages 1 and 2)

```sh
javac --release 17 -d out-test $(find src test -name "*.java")
java -cp out-test restaurant.StageOneChecks     # 52 checks passed, 0 failed
java -cp out-test restaurant.StageTwoChecks     # 38 checks passed, 0 failed
```

Plain Java, no test library. Run from the project root (some checks read `runs/` and `src/`). Each
check prints `PASS` or `FAIL`, and the program exits with code 1 if anything fails.

# Stage 2

## Stage 2 automated checks (`StageTwoChecks`)

| # | Requirement | Method | What is checked |
|---|-------------|--------|-----------------|
| 1 | VIP bill 10% off | `vipBillIsTenPercentOff` | Salad $8.10, Burger $13.50, Pasta $16.20; rounding to the nearest cent ($10.05 → $9.05, $10.04 → $9.04); Regular and Critic pay full price. |
| 2 | Loss rates 8 / 5 / 12 | `lossRatesPerType` | A Regular, VIP and Critic arrive on turns 1, 2, 3 and wait: after turn 3 they have 76 (3 × 8), 90 (2 × 5) and 88 (1 × 12). |
| 3 | Same flow for all types | `fullServiceForEachType` | The Section 3.3 Pasta script with a VIP gives 95, 90, 85, 85, 85 and pays $16.20 on turn 6 (85 recorded); with a Critic 88, 76, 64, 64, 64 and $18.00 (64 recorded). |
| 4 | LSP: satisfaction in 0..100 | `satisfactionStaysBetweenZeroAndHundred` | A waiting Critic goes 4 → 0 (not −8) on turn 9 and leaves exactly like a Regular (order cancelled, table freed); mixed games with seeds 1-30 keep every satisfaction in 0..100 at every turn end. |
| 5 | Critic special behaviour | `criticOrdersSignatureDish` | The Critic orders Pasta even when the food-choice rule says Salad (and for seeds 1-30); Regular and VIP follow the rule; on another menu the Critic picks the most expensive dish (first listed on a tie); the choice is displayed. |
| 6 | Polymorphic dispatch | `polymorphicDispatch` | The same calls on `Customer` references give `Regular 8 1500 Salad`, `VIP 5 1350 Salad`, `Critic 12 1500 Pasta`; the unchanged engine plays a full mixed game where the VIP pays the discounted bill and the Critic orders Pasta; a scan of `src` finds no `instanceof` / `getClass()` and no mention of `VIPCustomer` / `CriticCustomer` outside `ArrivalSchedule`. |
| 7 | Same base setting and targets | `sameBaseSettingAndTargets` | 18 turns, 2 tables, the same three targets, six arrivals on turns 1, 4, 7, 10, 13, 16 with one VIP, one Critic and four Regulars. |
| 8 | Stage 2 runs reproduce | `stageTwoRunsReproduce` | Replays both Stage 2 scripts and compares with the saved logs; checks the Critic records 64 vs 52 and the Regular 44 vs 52. |

## Stage 1 re-check

`StageOneChecks` still passes all 52 checks with the Stage 2 code. Its last check replays
`runs/victory.txt` and `runs/defeat.txt` with the all-Regular schedule and compares the output from
turn 1 to the end with the logs recorded by the Stage 1 build: identical (same Victory, same
Defeat). Only the two banner lines before turn 1 differ ("Stage 2" and the arrivals list). By hand:

```sh
java -cp out restaurant.Main --schedule regular --echo < runs/victory.txt | diff --strip-trailing-cr - runs/victory.log
java -cp out restaurant.Main --schedule regular --echo < runs/defeat.txt  | diff --strip-trailing-cr - runs/defeat.log
# only lines 1 and 3 (the banner) differ
```

## Stage 2 reproducible runs

Both use the **mixed schedule** (the default; `--schedule mixed`) and the **default seed 2002**:
C1 Regular (turn 1), **C2 Critic (turn 4)**, **C3 VIP (turn 7)**, C4 Regular (10), C5 Regular (13),
C6 Regular (16).

```sh
java -cp out restaurant.Main --echo < runs/stage2-critic-first.txt  | diff --strip-trailing-cr - runs/stage2-critic-first.log
java -cp out restaurant.Main --echo < runs/stage2-regular-first.txt | diff --strip-trailing-cr - runs/stage2-regular-first.log
```

**The situation.** The manager starts slowly (the Waiter waits on turns 1-2 and seats C1 on turn 3),
so on **turn 4** two customers need the Waiter at once: Regular C1 is seated waiting to order, and
Critic C2 has just arrived in the queue. Both runs make exactly the same choices on every turn
except this decision and the follow-through it forces on turns 5-8; turns 9-18 are identical.

| Turn | Critic first (`stage2-critic-first.txt`) | Regular first (`stage2-regular-first.txt`) |
|------|------------------------------------------|--------------------------------------------|
| 1-2 | Waiter waits (C1 92, 84) | same |
| 3 | seat C1 (C1 76) | same |
| 4 | C2 Critic arrives. **seat C2** → C1 68, C2 88 | C2 Critic arrives. **order C1** (Burger), cook C1 → C1 68, C2 88 waiting |
| 5 | order C2: **Critic chooses Pasta** (signature dish), cook 1/2 → C1 60, C2 76 | seat C2 → C1 60 (Burger READY), C2 76 |
| 6 | order C1 (Burger), cook C2 2/2 READY → C1 52, C2 64 | order C2: Pasta, cook 1/2 → C1 52, C2 64 |
| 7 | C3 VIP arrives. serve C2 (Critic stops losing at **64**), cook C1 → C1 44 | C3 VIP arrives. serve C1 (stops at **52**), cook C2 2/2 → C2 52 |
| 8 | serve C1 (stops at **44**) | serve C2 (stops at **52**) |
| 9-10 | C2 pays $18.00 (64), C1 pays $15.00 (44) | C1 pays $15.00 (52), C2 pays $18.00 (52) |
| 11 | C3 VIP orders Burger: **"VIP bill: $13.50"** | same |
| 14 | C3 pays **$13.50** (75) | same |
| 15-18 | C4 pays $9.00 (60); C5 served, C6 waiting at the end | same |

**Results**

| Run | Paid | Revenue | Recorded satisfaction | Average | Result |
|-----|------|---------|-----------------------|---------|--------|
| Critic first | 4 | $55.50 | C1 44, C2 Critic 64, C3 VIP 75, C4 60 | **60.8** | **VICTORY** |
| Regular first | 4 | $55.50 | C1 52, C2 Critic 52, C3 VIP 75, C4 60 | **59.8** | **DEFEAT** |

**Why the customer type decides it.** The one decision delays either C1 or the Critic by one turn.
Delaying the Regular costs 8 points; delaying the Critic costs 12. Serving the Critic first therefore
gains 12 − 8 = 4 points in total, which is +1.0 on the average of four paid customers: 60.8 instead
of 59.8, just across the target of 60. With identical Regulars the choice would not matter.
The VIP's 10% discount ($13.50 instead of $15.00) and slower loss (5 per turn: it waited 5 turns
in total, 3 of them in the queue, and still paid at 75) are visible in both logs.

# Stage 1

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
| 8 | Runs reproduce | `savedRunsReproduce` | Replays `runs/victory.txt` and `runs/defeat.txt` with seed 2002 and the all-Regular schedule and compares the output from turn 1 on with the saved logs (line endings ignored). |

## Stage 1 reproducible runs

Both runs use the base setting, the all-Regular schedule and the **default seed 2002**. With the
Stage 2 build, add `--schedule regular`.
Each input file has one command per line, answering the prompts in order (Waiter, then Chef,
every turn; an invalid line is followed by the replacement choice).

```sh
javac --release 17 -d out $(find src -name "*.java")
java -cp out restaurant.Main --schedule regular --echo < runs/victory.txt > my-victory.log
diff --strip-trailing-cr my-victory.log runs/victory.log     # only the banner lines differ
java -cp out restaurant.Main --schedule regular --echo < runs/defeat.txt > my-defeat.log
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
