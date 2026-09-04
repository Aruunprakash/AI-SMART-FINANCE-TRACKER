# Database — AI Smart Finance Tracker

Contains all datasets used for training and evaluating ML models.

## Structure

```
database/
├── raw/                      ← Original, unmodified source datasets
│   ├── Daily Household Transactions.csv   (primary training data for expense predictor)
│   ├── Personal_Finance_Dataset.csv
│   ├── sample_transactions.csv            (hand-labeled transactions for categorizer)
│   └── upi_dataset.csv
│
├── processed/                ← Generated / output files from model scripts
│   ├── sample_transactions_large.csv      (combined real + synthetic for training)
│   ├── monthly_expenses.csv
│   ├── monthly_expenses (1).csv
│   ├── monthly_expenses_for_prediction.csv
│   ├── weekly_expenses_for_prediction.csv
│   ├── prediction_model_results.csv
│   ├── transactions_with_anomaly_flags.csv
│   ├── weekly_prediction_model_results.csv
│   └── weekly_prediction_model_results - Copy.csv
│
└── upi_transactions/         ← UPI train/test split
    ├── financial_transaction_train.csv
    └── financial_transaction_test.csv
```

## Dataset Descriptions

| File | Location | Description |
|---|---|---|
| `Daily Household Transactions.csv` | `raw/` | Household expense records used to train the monthly expense predictor |
| `Personal_Finance_Dataset.csv` | `raw/` | Personal finance transactions dataset |
| `sample_transactions.csv` | `raw/` | Hand-labeled transactions for the categorization model |
| `upi_dataset.csv` | `raw/` | Raw UPI payment transaction data |
| `sample_transactions_large.csv` | `processed/` | Augmented dataset (real + synthetic) used for model training |
| `transactions_with_anomaly_flags.csv` | `processed/` | Output of the anomaly detection model |
| `upi_transactions/` | `upi_transactions/` | Train/test split of UPI transaction data |
