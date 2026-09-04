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

Use a Python 3.11 or 3.12 environment (`pip install -r model/requirements.txt`);
the pinned numpy/pandas have no wheels for 3.14.

```bash
cd model/training
python train_categorization_model_final.py   # categorizer + confusion matrix
python predict_expense.py                    # forecaster + RMSE/MAE
python anomaly_detection.py                  # anomaly flags + saved detector
```

Paths resolve from `paths.py`, so these work from any working directory.

## Notes

**`artifacts/` is git-ignored.** It previously held nine `.pkl`/`.joblib` files
saved with scikit-learn 0.24 that no longer load on scikit-learn 1.x, six of which
had no producing script at all. Trained models are build output — regenerate them
by running the scripts above. The old binaries remain in git history if needed.

**`train_categorization_model_final.py` is the reference for methodology.** It
augments 54 hand-labeled rows with synthetic brand data but tests only on held-out
**real** rows, specifically so the score isn't inflated by testing on the same
generator that produced the training data. Keep that property in any rewrite.

**`train_categorization_model1.py` was deleted.** It trained on
`database/upi_transactions/`, which turns out to be 29 unique templates repeated
~345 times with a deterministic template-to-label mapping -- a classifier scores
near 100% on it by memorising a lookup table. It also read that CSV with
`header=None` despite the file having a header, so the string `Transaction_Text`
was trained on as a sample labelled `Label`. See `database/README.md`.

**`predict_expense.py` now reports out-of-fold metrics.** It previously computed
MAE from predictions on the training rows, which understated the error; it now
uses `cross_val_predict` and reports both MAE and RMSE, written to
`evaluation/forecaster_metrics.json`.

**Sample size is the limitation to state in the report.** The categorizer's test
set is the held-out portion of 54 hand-labelled rows (~17 rows), so quote the
count next to any percentage.
