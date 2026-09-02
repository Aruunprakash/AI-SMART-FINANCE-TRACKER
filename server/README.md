# Server — AI Smart Finance Tracker Backend

> ⚠️ **Status: Not yet built.** This is a placeholder for the backend server.

## Planned Stack
- **Framework**: FastAPI (Python)
- **Model Loading**: `joblib` + `scikit-learn`
- **Runtime**: `uvicorn`

## Planned API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/` | Health check |
| `POST` | `/categorize` | Classify a transaction into a spending category |
| `POST` | `/anomaly` | Detect if a transaction amount is unusual |
| `POST` | `/predict` | Predict monthly expense for a given category |

## How to Run (once built)

```bash
cd server
pip install -r requirements.txt
uvicorn app.main:app --reload
```

## Model Artifacts Used

| Endpoint | Model File |
|---|---|
| `/categorize` | `../model/artifacts/expense_category_model.joblib` |
| `/anomaly` | Isolation Forest (to be trained + saved) |
| `/predict` | `../model/artifacts/final_expense_model.pkl` |
