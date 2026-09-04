# Model — AI Smart Finance Tracker

Contains all Python ML training scripts and trained model artifacts.

## Structure

```
model/
├── training/       ← Python scripts to train models
│   ├── train_categorization_model_final.py   (best classifier pipeline)
│   ├── train_categorization_model1.py        (earlier iteration)
│   ├── anomaly_detection.py                  (Isolation Forest anomaly detector)
│   └── predict_expense.py                    (RandomForest expense predictor)
│
└── artifacts/      ← Serialised trained models (.pkl / .joblib)
    ├── expense_category_model.joblib         (best transaction categorizer)
    ├── transaction_categorizer.pkl
    ├── expense_classifier_model.pkl
    ├── expense_prediction_model.pkl
    ├── final_expense_model.pkl               (monthly expense predictor)
    ├── category_expense_model.pkl
    ├── category_monthly_model.pkl
    ├── monthly_expense_model.pkl
    └── weekly_expense_model.pkl
```

## Models Overview

| Model | Script | Artifact | Task |
|---|---|---|---|
| Transaction Categorizer | `train_categorization_model_final.py` | `expense_category_model.joblib` | Classify merchant text → category |
| Anomaly Detector | `anomaly_detection.py` | *(IsolationForest, to be persisted)* | Flag unusual transaction amounts |
| Expense Predictor | `predict_expense.py` | `final_expense_model.pkl` | Predict monthly spend by category |

## Usage

```bash
cd model/training
python train_categorization_model_final.py   # trains & saves categorizer
python predict_expense.py                    # trains & saves expense predictor
python anomaly_detection.py                  # runs anomaly detection
```
