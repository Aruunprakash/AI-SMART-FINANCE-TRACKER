# Model — AI Smart Finance Tracker

Python training scripts for the three ML models.

## Structure

```
model/
├── training/
│   ├── train_categorization_model_final.py   ← current categorizer
│   ├── train_categorization_model1.py        ← earlier iteration (see Notes)
│   ├── anomaly_detection.py                  ← Isolation Forest anomaly detector
│   └── predict_expense.py                    ← RandomForest monthly expense forecaster
│
└── artifacts/      ← generated output, git-ignored (see Notes)
```

## Models

| Model | Script | Task |
|---|---|---|
| Transaction Categorizer | `train_categorization_model_final.py` | merchant text → one of 10 categories |
| Anomaly Detector | `anomaly_detection.py` | flag unusual transaction amounts |
| Expense Forecaster | `predict_expense.py` | predict monthly spend per category |

## Usage

> **Known issue:** all four scripts currently use bare relative filenames
> (`pd.read_csv("sample_transactions.csv")`) but the datasets live under
> `database/`, and they write artifacts to the current working directory
> rather than `artifacts/`. The commands below will fail until those paths
> are fixed. This is tracked as the next change to these scripts.

```bash
cd model/training
python train_categorization_model_final.py
python predict_expense.py
python anomaly_detection.py
```

## Notes

**`artifacts/` is git-ignored.** It previously held nine `.pkl`/`.joblib` files
saved with scikit-learn 0.24 that no longer load on scikit-learn 1.x, six of which
had no producing script at all. Trained models are build output — regenerate them
by running the scripts above. The old binaries remain in git history if needed.

**`train_categorization_model_final.py` is the reference for methodology.** It
augments 54 hand-labeled rows with synthetic brand data but tests only on held-out
**real** rows, specifically so the score isn't inflated by testing on the same
generator that produced the training data. Keep that property in any rewrite.

**`train_categorization_model1.py` is superseded** and has a known bug: it reads
`financial_transaction_test.csv` with `header=None`, but that file does have a
header, so the literal string `Transaction_Text` is trained on as a sample labeled
`Label`. It is kept only because it is the sole script that touches the 10,000-row
UPI dataset, which is the intended training source for the next categorizer.

**`predict_expense.py` reports an in-sample MAE.** Its R² is cross-validated, but
the MAE on line 50 is computed from predictions on the training data and is
therefore optimistic. Use `cross_val_predict` for an honest figure.
