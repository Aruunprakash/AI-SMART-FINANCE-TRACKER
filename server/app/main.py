"""
AI Smart Finance Tracker — FastAPI Backend (Placeholder)
=========================================================
This server is yet to be built. Below is the planned skeleton.
All endpoints will interface with the ML model artifacts in ../model/artifacts/
"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(
    title="AI Smart Finance Tracker API",
    description="Backend server for the AI Smart Finance Tracker Android app.",
    version="0.1.0"
)


# ── Request/Response schemas ──────────────────────────────────────────────────

class TransactionRequest(BaseModel):
    merchant_text: str
    amount: float


class CategoryResponse(BaseModel):
    category: str
    confidence: float


class AnomalyResponse(BaseModel):
    amount: float
    status: str   # "normal" | "UNUSUAL"


class PredictionRequest(BaseModel):
    month: int           # 1-12
    category: str


class PredictionResponse(BaseModel):
    category: str
    predicted_amount: float


# ── Endpoints (stubs) ─────────────────────────────────────────────────────────

@app.get("/")
async def health():
    """Health check endpoint."""
    return {"status": "ok", "message": "AI Smart Finance Tracker API is running."}


@app.post("/categorize", response_model=CategoryResponse)
async def categorize_transaction(req: TransactionRequest):
    """
    TODO: Load model/artifacts/expense_category_model.joblib
    and classify the merchant_text into a category.
    Categories: Food, Groceries, Travel, Shopping, Bills,
                Healthcare, Entertainment, Investment, Rent, Transfer
    """
    raise NotImplementedError("Server not yet built — model integration pending.")


@app.post("/anomaly", response_model=AnomalyResponse)
async def detect_anomaly(req: TransactionRequest):
    """
    TODO: Load Isolation Forest model and flag unusual transactions.
    Returns 'UNUSUAL' if the transaction amount is an outlier.
    """
    raise NotImplementedError("Server not yet built — model integration pending.")


@app.post("/predict", response_model=PredictionResponse)
async def predict_expense(req: PredictionRequest):
    """
    TODO: Load model/artifacts/final_expense_model.pkl
    and predict monthly expense for a given category and month.
    """
    raise NotImplementedError("Server not yet built — model integration pending.")
