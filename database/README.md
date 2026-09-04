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
| `upi_transactions/financial_transaction_train.csv` | 10,000 | *(unused - see warning)* | Synthetic UPI text, 5 labels. Only **29 distinct templates** repeated ~345x each |
| `upi_transactions/financial_transaction_test.csv` | 1,000 | *(unused)* | Drawn from the same 29 templates - not a genuine held-out set |
| `processed/sample_transactions_large.csv` | 654 | `anomaly_detection.py` | Real + synthetic rows, written by `train_categorization_model_final.py` |
| `processed/transactions_with_anomaly_flags.csv` | 654 | — | Output of `anomaly_detection.py` |
| `processed/*expenses*.csv`, `processed/*results*.csv` | — | — | Aggregation / evaluation output |

## Notes

- **`processed/` is generated.** Everything in it is reproducible by re-running the
  scripts in `model/training/`. Only `raw/` and `upi_transactions/` are source data.
- **Do not train on `upi_transactions/`.** It looks like 10,000 labelled rows,
  but stripping the `| Ref:... | Amount:...` suffix leaves only **29 unique
  merchant strings**, each repeated ~345 times, with a perfectly deterministic
  template-to-label mapping. Any train/test split shares the same 29 templates,
  so a classifier trained on it scores near 100% while having memorised a
  29-entry lookup table. The 1,000-row "test" file has the same problem. It also
  carries only 5 labels (Food, Travel, Shopping, Investment, EMI) against the
  app's 10 categories.

  Verify this yourself:
  `python -c "import csv,collections;rows=list(csv.DictReader(open('database/upi_transactions/financial_transaction_train.csv')));print(len(set(r['Transaction_Text'].split('|')[0].strip() for r in rows)))"`

- **`raw/sample_transactions.csv` (54 rows) remains the only genuine labelled
  data.** It is small, and that is the honest limitation to state in the report --
  the real held-out test set is ~17 rows. Augmenting it with synthetic brand
  variations (as `train_categorization_model_final.py` does) while testing only
  on held-out real rows is the correct approach given that constraint.

- CSVs are stored via **Git LFS** (see `.gitattributes`). Run `git lfs pull` after
  cloning if the files look like short text pointers.
