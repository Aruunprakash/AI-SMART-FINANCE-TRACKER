# AI Smart Finance Tracker

An AI-powered personal finance tracker that automatically categorises UPI/SMS transactions, detects spending anomalies, and forecasts future expenses.

## Project Structure

```
AI-SMART-FINANCE-TRACKER/
│
├── client/          ← Android app (Kotlin + Jetpack Compose)
├── server/          ← Backend REST API (FastAPI — in progress)
├── model/           ← ML training scripts & model artifacts
│   ├── training/    ← Python training scripts
│   └── artifacts/   ← Serialised .pkl / .joblib model files
└── database/        ← Datasets for training & evaluation
    ├── raw/         ← Original source CSVs
    ├── processed/   ← Generated / output CSVs
    └── upi_transactions/
```

## Components

### `client/` — Android App
Jetpack Compose Android application that reads SMS/notification data, categorises transactions locally, and displays a dashboard with spending insights.

### `server/` — Backend API *(In Progress)*
FastAPI backend that exposes ML model predictions as REST endpoints. Planned endpoints: `/categorize`, `/anomaly`, `/predict`.

### `model/` — Machine Learning
- **Transaction Categorizer** — TF-IDF + Logistic Regression / Naive Bayes / Random Forest pipeline classifying merchant text into 10 spending categories.
- **Anomaly Detector** — Isolation Forest flagging unusual transaction amounts.
- **Expense Predictor** — Random Forest regressor forecasting monthly spend per category.

### `database/` — Datasets
Raw and processed CSV datasets used to train and evaluate the ML models.

## Tech Stack

| Layer | Technology |
|---|---|
| Android Client | Kotlin, Jetpack Compose, Room, Firebase |
| Backend Server | Python, FastAPI, Uvicorn *(planned)* |
| ML Models | scikit-learn, joblib, pandas, numpy |
| Database (local) | Room (SQLite) on Android |