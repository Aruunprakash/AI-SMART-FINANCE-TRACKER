# Database — AI Smart Finance Tracker

Datasets used for training and evaluating the ML models.

## Structure

```
database/
├── raw/                      ← Original, unmodified source datasets
│   ├── Daily Household Transactions.csv
│   ├── Personal_Finance_Dataset.csv
│   └── sample_transactions.csv
│
├── processed/                ← Generated output from the scripts in model/training/
│   ├── sample_transactions_large.csv
│   ├── transactions_with_anomaly_flags.csv
│   ├── monthly_expenses.csv
│   ├── monthly_expenses_for_prediction.csv
│   ├── weekly_expenses_for_prediction.csv
│   ├── prediction_model_results.csv
│   └── weekly_prediction_model_results.csv
│
└── upi_transactions/         ← UPI train/test split
    ├── financial_transaction_train.csv
    └── financial_transaction_test.csv
```

## Datasets

| File | Rows | Used by | Description |
|---|---|---|---|
| `raw/Daily Household Transactions.csv` | 2,461 | `predict_expense.py` | Household expense records; source for the monthly expense forecaster |
| `raw/sample_transactions.csv` | 54 | `train_categorization_model_final.py` | Hand-labeled merchant text → category, across 10 categories |
| `raw/Personal_Finance_Dataset.csv` | 1,500 | *(unused)* | Personal finance transactions; not currently referenced by any script |
| `upi_transactions/financial_transaction_train.csv` | 10,000 | `train_categorization_model1.py` | Labeled UPI transaction text — the largest real labeled dataset here |
| `upi_transactions/financial_transaction_test.csv` | 1,000 | — | Held-out split of the above |
| `processed/sample_transactions_large.csv` | 654 | `anomaly_detection.py` | Real + synthetic rows, written by `train_categorization_model_final.py` |
| `processed/transactions_with_anomaly_flags.csv` | 654 | — | Output of `anomaly_detection.py` |
| `processed/*expenses*.csv`, `processed/*results*.csv` | — | — | Aggregation / evaluation output |

## Notes

- **`processed/` is generated.** Everything in it is reproducible by re-running the
  scripts in `model/training/`. Only `raw/` and `upi_transactions/` are source data.
- **`financial_transaction_train.csv` is the most valuable dataset here** and is
  currently underused — the categorizer trains on 54 real rows while 10,000 labeled
  rows sit unused. Its text column packs three fields into one string:
  `Swiggy order payment | Ref:0f8f77bb | Amount: INR 37090.45`. Split on `|` and keep
  the first segment before vectorizing, or TF-IDF will learn from reference hashes
  and amount digits.
- CSVs are stored via **Git LFS** (see `.gitattributes`). Run `git lfs pull` after
  cloning if the files look like short text pointers.
