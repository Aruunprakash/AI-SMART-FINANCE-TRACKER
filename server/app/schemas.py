from pydantic import BaseModel


class TransactionRequest(BaseModel):
    merchant_text: str
    amount: float


class CategoryResponse(BaseModel):
    category: str
    confidence: float


class AnomalyResponse(BaseModel):
    amount: float
    status: str  # "normal" | "UNUSUAL"


class PredictionRequest(BaseModel):
    month: int  # 1-12
    category: str


class PredictionResponse(BaseModel):
    category: str
    predicted_amount: float
