# FinSet Pacer (finance-tracker-mobile)

An early Android prototype (September 2026) that reads bank and UPI SMS on the
phone and tells you how many days to wait before your next outing, so you
still hit your monthly savings target.

This idea was later rebuilt as [Pinch](https://github.com/suhasisnice/pinch).
This repository is kept for reference.

## What it does

- Asks for SMS permission, scans this month's inbox, then listens for new
  bank SMS as they arrive. Each message is stored once (deduplicated by hash).
- A regex parser pulls out the amount and merchant and sorts it into a
  category.
- The pacing engine compares your spending with your allowance and savings
  goal and says Safe, Hold off, or Budget exceeded.
- Suggests a typical outing cost from the last 6 months of food and
  entertainment spending.
- Screens: permission, onboarding, dashboard, transactions, settings.

## Tech stack

Kotlin, Jetpack Compose, Room, Hilt. Unit tests for the pacing engine, the
outing-cost estimate and the tracking window (`app/src/test`).

## Build

Open the folder in Android Studio and run on a phone with Android 8.0
(API 26) or later. SMS reading needs a real phone.
