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
- [x] **Build verified**: installed Android Studio, Microsoft OpenJDK 17, and
      the Android SDK (cmdline-tools, platform-tools, platforms;android-34,
      build-tools;34.0.0) under `C:\Android\Sdk` (moved off the default
      `%LOCALAPPDATA%` path because it contains spaces, which broke the SDK's
      `.bat` tooling). Generated the Gradle wrapper with a standalone Gradle
      8.7. `./gradlew testDebugUnitTest` now passes: **14/14 tests green**
      (6 `PacerEngineTest`, 8 `SmsParserTest`), and `compileDebugKotlin`
      (the whole app, not just tests) compiles clean.
      Fixed along the way: missing `padding` import in `FinSetNavHost.kt`;
      three screens needed `@OptIn(ExperimentalMaterial3Api::class)` for
      `TopAppBar`; `SettingsForm.kt` used `ExposedDropdownMenu`, which doesn't
      exist in the Material3 version this BOM pulls (1.2.x) — swapped for
      plain `DropdownMenu` + `DropdownMenuItem`.
- [ ] Not yet run on a device/emulator — no AVD created yet. Unit tests +
      compile success confirm the code is correct, not the on-device SMS →
      dashboard flow.
- [ ] Launcher icon is a placeholder vector (green square, 3 bars) — swap for
      real branding whenever that exists.
- [ ] No app-level error/empty states beyond the basics (e.g. malformed
      settings, permission permanently denied) — add if real usage surfaces them.

## Next steps
1. Open Android Studio (now installed) and let it index this project — it
   should pick up the existing `C:\Android\Sdk` and JDK 17 automatically.
2. Create an AVD (or plug in a device) and run the app; grant SMS permission
   and confirm onboarding → dashboard flow with a real or
   `adb emu sms send <sender> "<body>"` test message.
3. Decide on real app icon / branding.

## Local toolchain (this machine)
- JDK 17: `C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot`
- Android SDK: `C:\Android\Sdk` (NOT the default `%LOCALAPPDATA%\Android\Sdk`
  — moved to avoid a space in the username breaking SDK `.bat` scripts)
- Standalone Gradle 8.7 (used once to generate the wrapper): `C:\Android\gradle-8.7`
- Project's own `local.properties` points `sdk.dir` at `C:\Android\Sdk`
  (gitignored, machine-specific — regenerate if this repo is cloned elsewhere)
