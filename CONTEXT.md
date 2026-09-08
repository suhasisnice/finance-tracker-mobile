# FinSet Pacer — Co-working Context

Living handoff doc for this project. Update it as work progresses, not just at
milestones, so any session (or person) can pick this up cold.

## What this is
Android app (native Kotlin + Jetpack Compose) that reads incoming bank/UPI SMS
on-device, tracks spending in real time, and tells the user how many days to
wait before an outing so they don't miss their monthly savings target. Full
spec: see the original master spec pasted into the session that started this
(§ references throughout the code point back to it — §3 pacing math, §4 SMS
parsing, §5 schema).

## Repo / version control
- This folder (`FinSetPacer/`) is its own git repo, **separate** from the
  parent `financetracker` repo (which holds an unrelated old web ledger).
- Remote: `https://github.com/suhasisnice/finance-tracker-mobile.git`
- **Guardrail**: if session/context usage ever nears 95%, commit and push
  whatever is in a working state immediately rather than risk losing work.
  Apply this in every session working on this repo.

## Architecture
- **DI**: Hilt. **DB**: Room (`AppDatabase`, `SettingsEntity` singleton row id=1,
  `TransactionEntity` with UNIQUE `smsHash` for dedup).
- **Domain** (`domain/PacerEngine.kt`): pure Kotlin, no Android deps, implements
  spec §3.A/B/C literally. Sealed `PacerResult`: `Safe` / `HoldOff` / `BudgetExceeded`.
- **SMS** (`sms/`): `SmsParser.kt` (regex extraction per §4, pure Kotlin),
  `SmsReceiver.kt` (BroadcastReceiver for real-time SMS_RECEIVED, Hilt field
  injection), `SmsInboxScanner.kt` (one-time backfill of the current month's
  inbox right after permission is granted, since without it the dashboard
  would show ₹0 until the next SMS arrives — not explicitly in the spec but
  needed for correctness).
- **UI**: `ui/root/AppRoot.kt` is the top-level router — no settings row yet →
  `OnboardingScreen`; settings but no SMS permission → `SmsPermissionScreen`;
  else → `FinSetNavHost` (bottom nav: Dashboard / Transactions / Settings).
  Onboarding and Settings share one form (`ui/settings/SettingsForm.kt` +
  `SettingsFormState`) to avoid duplicating the input fields.
- `data/repository/*` mirrors the old FastAPI ledger's month-window query
  pattern (`start = first-of-month`, `end = start + 1 month`) — see
  `domain/model/MonthWindow.kt` — rather than re-deriving it.

## Status
- [x] Full project skeleton: Gradle config, manifest, Room DB, Hilt DI, SMS
      parser + receiver + backfill scanner, PacerEngine, all 5 screens
      (onboarding/permission/dashboard/transactions/settings), theming.
- [x] JVM unit tests: `PacerEngineTest.kt` (§3 math, all 3 UI states),
      `SmsParserTest.kt` (regex extraction against GPay/PhonePe/HDFC/Axis/SBI
      sample bodies, dedup hash stability, non-debit rejection).
- [ ] **Not verified by build** — no JDK/Android SDK/Gradle installed on the
      machine this was authored on. Needs Android Studio to sync (fetches its
      own Gradle + generates `gradle-wrapper.jar`, which isn't checked in here
      since it can't be produced without a local Gradle install), then
      `./gradlew testDebugUnitTest` to confirm the unit tests actually pass,
      then a device/emulator run to confirm the SMS → dashboard flow end-to-end.
- [ ] Launcher icon is a placeholder vector (green square, 3 bars) — swap for
      real branding whenever that exists.
- [ ] No app-level error/empty states beyond the basics (e.g. malformed
      settings, permission permanently denied) — add if real usage surfaces them.

## Next steps
1. Open in Android Studio, let Gradle sync, run unit tests.
2. Grant SMS permission on a device/emulator and confirm onboarding → dashboard
   flow with a real or `adb emu sms send` test message.
3. Decide on real app icon / branding.
