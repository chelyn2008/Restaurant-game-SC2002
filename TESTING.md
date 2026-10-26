# Testing (Stages 1, 2 and 3)

```sh
javac --release 17 -d out-test $(find src test -name "*.java")
java -cp out-test restaurant.StageOneChecks     # 52 checks passed, 0 failed
java -cp out-test restaurant.StageTwoChecks     # 38 checks passed, 0 failed
java -cp out-test restaurant.StageThreeChecks   # 48 checks passed, 0 failed
```

Plain Java, no test library. Run from the project root (some checks read `runs/` and `src/`). Each
check prints `PASS` or `FAIL`, and the program exits with code 1 if anything fails.
All three suites pass on the final (Stage 3) code.

All runs replay with `java -cp out restaurant.Main --stage N --echo < runs/<script>.txt`
(in PowerShell: `Get-Content runs\<script>.txt | java -cp out restaurant.Main --stage N --echo`).

| Script | Stage | Seed | Result | Log |
|--------|-------|------|--------|-----|
| `runs/victory.txt` | `--stage 1` | 2002 | VICTORY | `runs/victory.log` |
| `runs/defeat.txt` | `--stage 1` | 2002 | DEFEAT | `runs/defeat.log` |
| `runs/stage2-critic-first.txt` | `--stage 2` | 2002 | VICTORY | `runs/stage2-critic-first.log` |
| `runs/stage2-regular-first.txt` | `--stage 2` | 2002 | DEFEAT | `runs/stage2-regular-first.log` |
| `runs/stage3-victory.txt` | `--stage 3` (default) | 2002 | VICTORY | `runs/stage3-victory.log` |
| `runs/stage3-defeat.txt` | `--stage 3` (default) | 2002 | DEFEAT | `runs/stage3-defeat.log` |

# Stage 3

## Stage 3 automated checks (`StageThreeChecks`)

| # | Requirement | Method | What is checked |
|---|-------------|--------|-----------------|
| 1 | Host + Waiter seat in one turn | `hostAndWaiterSeatInOneTurn` | Turn 2: the Host seats C1 at Table 1 and the Waiter seats C2 at Table 2. The Host cannot seat twice in a turn (the Waiter still can); an invalid Host choice (no free table) gives the reason and does not use the Host's task; `order C1` is not a Host command. |
| 2 | Combo units and price | `comboUnitsAndPrice` | Salad + Pasta $24.30 / 3 units, Salad + Burger $21.60 / 2, Burger + Pasta $29.70 / 3; rounding ($19.05 × 0.9 = $17.145 → $17.15); the six-item Stage 3 menu; the Stage 1/2 menu unchanged; the Chef takes a combo 1/3 → 2/3 → 3/3 READY → SERVED; serving before READY is refused; the choice is displayed; VIP 10% off a combo ($21.87). |
| 3 | Once per game | `happyHourOncePerGame` | A second `activate` is refused with a reason; the engine stops offering Happy Hour once used. |
| 4 | Duration of two turns | `happyHourLastsTwoTurns` | Activated on turn 5: active on 5 and 6 only; remaining turns 2, 1, 0; activated on turn 18: active on 18. |
| 5 | +15 recovery, max 100 | `happyHourRecovery` | Queued Regular: 76 → +15 → 83 at the end of turn 4, 90 after turn 5, no recovery on turn 6 (82). A customer at 92 recovers to 100, not 107. A served customer and a customer who has left do not recover. |
| 6 | 20% only while active | `discountOnlyForOrdersTakenWhileActive` | Burgers ordered on turns 4 / 5 / 6 / 7 with Happy Hour on turns 5-6: $15.00 / $12.00 / $12.00 / $15.00; the order placed before activation keeps $15.00. |
| 7 | VIP first, then Happy Hour | `vipDiscountFirstThenHappyHour` | VIP Burger during Happy Hour = $10.80, labelled "VIP + Happy Hour"; Regular $12.00; VIP after Happy Hour $13.50; with rounding the order matters (35c → 26c; the reverse order would give 25c). |
| 8 | Price locked after Happy Hour | `priceStaysLockedAfterHappyHour` | A VIP orders a Burger on turn 1 during Happy Hour ($10.80 shown), pays on turn 4 after it has ended: the Cashier collects $10.80. |
| 9 | Base setting and stage switches | `sameBaseSettingAndFeatureSwitches` | Stage 3: 18 turns, 2 tables, six arrivals, same targets, Host + Happy Hour + combos; `--stage 1` and `--stage 2` have none of them. |
| 10 | Stage 3 runs reproduce | `stageThreeRunsReproduce` | Replays both Stage 3 scripts against their logs; VIP Burger $10.80 vs $13.50; Critic 43 vs 28 at payment; C1's Salad + Pasta combo. |

## Earlier stages re-checked on the Stage 3 code

- `StageOneChecks`: 52 passed. Its replay of `runs/victory.txt` / `runs/defeat.txt` with the Stage 1
  setting matches the Stage 1 logs from turn 1 to the end.
- `StageTwoChecks`: 38 passed. Its replay of both Stage 2 scripts matches the saved Stage 2 logs.
  Those two logs were re-recorded after the welcome banner was restyled in Stage 3; compared with the
  logs at the `stage-2` tag, only the banner lines differ (title, blank lines), and every turn, choice
  and result is the same.
- By hand: `--stage 2` replays of the Stage 2 scripts show no `diff` against `runs/`; `--stage 1`
  replays of the Stage 1 scripts differ only in the banner lines before turn 1.

## Stage 3 reproducible runs

Stage 3 (the default, `--stage 3`), **seed 2002**, arrivals C1 Regular (1), C2 Critic (4), C3 VIP (7),
C4 Regular (10), C5 Regular (13), C6 Regular (16). Menu: Salad, Burger, Pasta and the three combos.

```sh
java -cp out restaurant.Main --echo < runs/stage3-victory.txt | diff --strip-trailing-cr - runs/stage3-victory.log
java -cp out restaurant.Main --echo < runs/stage3-defeat.txt  | diff --strip-trailing-cr - runs/stage3-defeat.log
```

Each turn's lines are: the Happy Hour answer (only while it is unused), then the Host, Waiter and
Chef commands; an invalid line is followed by the replacement. With seed 2002 the food choices, in
the order orders are taken, are: C1 Salad + Pasta Combo, C2 (Critic, signature dish) Burger + Pasta
Combo, C3 Burger, C4 Salad + Burger Combo, C5 Salad + Pasta Combo, C6 Pasta.

**The situation.** A manager whose Waiter starts slowly (it waits on turns 1-3) but who uses the
Host. **The only difference between the two scripts is the Happy Hour answer on turn 10**: `yes` in
`stage3-victory.txt`, `no` in `stage3-defeat.txt` (which then keeps answering `no`, because the
question is asked every turn until Happy Hour is used).

| Turn | Both runs | Victory run (Happy Hour on turn 10) | Defeat run (never) |
|------|-----------|-------------------------------------|--------------------|
| 1 | C1 arrives. Host `seat C2` is **rejected** ("There is no customer C2", task not used); Host seats C1. | | |
| 4 | C2 Critic arrives. **Host seats C2 while the Waiter takes C1's order**: C1 orders the **Salad + Pasta Combo $24.30 (3 units)**; Chef 1/3. | | |
| 5 | Waiter takes C2's order: Critic's signature dish **Burger + Pasta Combo $29.70**; Chef 2/3 for C1. | | |
| 6-7 | C1's combo READY on 6, served on 7. C3 VIP arrives on 7. | | |
| 9 | C1 pays $24.30 (52); C2's combo READY (Critic at 28). | | |
| 10 | C4 arrives. Host seats C3; Waiter serves C2. | **Happy Hour ON** ("2 turns left"). Recovery: C2 28 → 43, C3 → 100, C4 100. | No recovery: C2 stays at 28. |
| 11 | Waiter takes C3's order: VIP chooses Burger. | **"VIP + Happy Hour bill: $10.80"** ("1 turn left"); C3, C4 recover again. | "VIP bill: $13.50". |
| 12 | C2 pays $29.70. | Critic records **43** | Critic records **28** |
| 13 | Host seats C4 and the Waiter takes C4's order in the same turn: Salad + Burger Combo $21.60. | | |
| 14 | C3 pays. | **$10.80** (locked, Happy Hour ended on 11), satisfaction 95 | $13.50, satisfaction 75 |
| 17 | C4 pays $21.60. | satisfaction 68 | satisfaction 60 |
| 18 | C5 and C6 still ordered at the end. | | |

**Results**

| Run | Paid | Revenue | Recorded satisfaction (C1, C2, C3, C4) | Average | Result |
|-----|------|---------|----------------------------------------|---------|--------|
| `stage3-victory` | 4 | $86.40 | 52, 43, 95, 68 | **64.5** | **VICTORY** |
| `stage3-defeat` | 4 | $89.10 | 52, 28, 75, 60 | **53.8** | **DEFEAT** |

**How the choice changes the result.** Happy Hour costs revenue (the VIP's Burger is $2.70 cheaper)
but buys satisfaction: +15 for the Critic just before being served, and +15 twice for the VIP and
C4 while they wait. The average rises by 10.7 points and crosses 60. This pair is also the scenario
for the sequence diagram (arrival, order, Host/Waiter/Chef, READY → SERVED, eating, payment, result
check, and VIP + Happy Hour pricing).

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
java -cp out restaurant.Main --stage 1 --echo < runs/victory.txt | diff --strip-trailing-cr - runs/victory.log
java -cp out restaurant.Main --stage 1 --echo < runs/defeat.txt  | diff --strip-trailing-cr - runs/defeat.log
# only the banner lines before "TURN 1" differ (the logs were recorded by the Stage 1 build)
```

## Stage 2 reproducible runs

Both use the **Stage 2 game** (`--stage 2`: mixed arrivals, base menu, no Host, no Happy Hour) and the
**default seed 2002**: C1 Regular (turn 1), **C2 Critic (turn 4)**, **C3 VIP (turn 7)**, C4 Regular (10),
C5 Regular (13), C6 Regular (16). (With the Stage 2 build these ran with no option, as `--schedule mixed`.)

```sh
java -cp out restaurant.Main --stage 2 --echo < runs/stage2-critic-first.txt  | diff --strip-trailing-cr - runs/stage2-critic-first.log
java -cp out restaurant.Main --stage 2 --echo < runs/stage2-regular-first.txt | diff --strip-trailing-cr - runs/stage2-regular-first.log
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
final build, add `--stage 1` (the Stage 2 build used `--schedule regular`; the Stage 1 build needed no option).
Each input file has one command per line, answering the prompts in order (Waiter, then Chef,
every turn; an invalid line is followed by the replacement choice).

```sh
javac --release 17 -d out $(find src -name "*.java")
java -cp out restaurant.Main --stage 1 --echo < runs/victory.txt > my-victory.log
diff --strip-trailing-cr my-victory.log runs/victory.log     # only the banner lines differ
java -cp out restaurant.Main --stage 1 --echo < runs/defeat.txt > my-defeat.log
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
