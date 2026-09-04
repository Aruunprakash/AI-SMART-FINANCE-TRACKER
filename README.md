# AI Smart Finance Tracker

An AI-powered personal finance tracker that captures UPI/bank transaction
notifications on Android, categorises them, flags spending anomalies, and
forecasts future expenses.

## Project Structure

```
AI-SMART-FINANCE-TRACKER/
│
├── client/          ← Android app (Kotlin + Jetpack Compose)
├── server/          ← FastAPI REST API serving the three models
├── model/           ← ML training scripts
│   ├── training/
│   └── artifacts/   ← generated, git-ignored
└── database/        ← Datasets
    ├── raw/
    ├── processed/   ← generated
    └── upi_transactions/
```

## How it fits together

```
Notification (bank / UPI app)
  → ExpenseNotificationListener → ExpenseParser (regex)
  → ExpenseRepository.captureExpense()
      ├─ Room insert (local, always — survives server being down)
      ├─ POST /categorize → category
      ├─ POST /anomaly    → unusual flag
      └─ Firestore sync (if signed in)
  → Dashboard renders from the Room Flow
```

## Components

### `client/` — Android App
Jetpack Compose app that reads **notifications** (not SMS) from payment apps,
parses amount and merchant with regex, stores transactions in Room, and enriches
them by calling the server.

### `server/` — Backend API
FastAPI backend exposing `/categorize`, `/anomaly`, and `/predict`.
See [server/README.md](server/README.md) for setup and endpoint details.

### `model/` — Machine Learning
- **Transaction Categorizer** — TF-IDF + Logistic Regression / Naive Bayes / Random Forest, classifying merchant text into 10 categories.
- **Anomaly Detector** — Isolation Forest over transaction amounts.
- **Expense Forecaster** — Random Forest regressor predicting monthly spend per category.

### `database/` — Datasets
Raw and processed CSVs, stored via Git LFS. Run `git lfs pull` after cloning.

## Tech Stack

| Layer | Technology |
|---|---|
| Android Client | Kotlin, Jetpack Compose, Room, Retrofit, Firebase |
| Backend Server | Python, FastAPI, Uvicorn |
| ML Models | scikit-learn, joblib, pandas, numpy |
| Local storage | Room (SQLite) on device |

## Current state

Honest status, so nothing here is a surprise:

- **The Android app does not build yet.** `client/` is missing its Gradle
  scaffolding (`settings.gradle.kts`, root `build.gradle.kts`, wrapper), the app
  build file has a corrupted name, `navigation-compose` is imported but not
  declared, and the `google-services` plugin is not applied so Firebase will not
  initialise.
- **The server runs and returns real predictions**, but its models are trained on
  synthetic data by `train_server_models.py`. Replacing them with models trained
  on the real datasets is the main outstanding ML work.
- **Most screens under `client/.../ui/` are unwired mockups** with hardcoded
  sample data, and are not currently reachable — there is no `NavHost`, and
  `MainActivity` renders only the dashboard.
- `requirements.txt` pins `numpy 1.26.4` / `pandas 2.2.2`, which have no wheels
  for Python 3.14 — use a 3.11 or 3.12 virtual environment.
