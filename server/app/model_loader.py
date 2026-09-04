"""
Loads the trained model artifacts once at server startup and exposes
simple inference functions. Keeping this separate from main.py means the
endpoint handlers stay thin, and models are only ever loaded once
(loading a joblib file is slow — you don't want to do it per-request).
"""

import json
from pathlib import Path

import joblib
import numpy as np
import pandas as pd

MODELS_DIR = Path(__file__).resolve().parent.parent / "models"

_categorizer = None
_anomaly_model = None
_forecaster = None
_forecaster_columns = None


class ModelLoadError(RuntimeError):
    """Raised when a model file is missing or fails to unpickle."""


def load_all_models() -> None:
    """Call once at server startup (see main.py's startup event)."""
    global _categorizer, _anomaly_model, _forecaster, _forecaster_columns

    try:
        _categorizer = joblib.load(MODELS_DIR / "categorizer.joblib")
    except FileNotFoundError as e:
        raise ModelLoadError(
            "categorizer.joblib not found. Run train_server_models.py first."
        ) from e
    except Exception as e:
        raise ModelLoadError(
            f"Failed to load categorizer.joblib ({e}). This usually means it "
            "was saved with a different scikit-learn version — retrain it "
            "with train_server_models.py using your current environment."
        ) from e

    try:
        _anomaly_model = joblib.load(MODELS_DIR / "anomaly.joblib")
    except FileNotFoundError as e:
        raise ModelLoadError(
            "anomaly.joblib not found. Run train_server_models.py first."
        ) from e

    try:
        _forecaster = joblib.load(MODELS_DIR / "forecaster.joblib")
        with open(MODELS_DIR / "forecaster_columns.json") as f:
            _forecaster_columns = json.load(f)
    except FileNotFoundError as e:
        raise ModelLoadError(
            "forecaster.joblib / forecaster_columns.json not found. "
            "Run train_server_models.py first."
        ) from e


def models_ready() -> bool:
    return all([_categorizer, _anomaly_model, _forecaster, _forecaster_columns])


def predict_category(merchant_text: str) -> tuple[str, float]:
    """Returns (category, confidence 0-1)."""
    category = _categorizer.predict([merchant_text])[0]
    proba = _categorizer.predict_proba([merchant_text])[0]
    confidence = float(np.max(proba))
    return category, confidence


def predict_anomaly(amount: float) -> str:
    """Returns 'normal' or 'UNUSUAL'."""
    flag = _anomaly_model.predict([[amount]])[0]  # -1 = anomaly, 1 = normal
    return "UNUSUAL" if flag == -1 else "normal"


def predict_monthly_amount(month: int, category: str) -> float:
    """Returns the predicted spend for a given month + category."""
    input_row = pd.DataFrame(0, index=[0], columns=_forecaster_columns)
    if "Month" in input_row.columns:
        input_row["Month"] = month

    cat_col = f"Category_{category}"
    if cat_col not in input_row.columns:
        known = sorted(
            c.replace("Category_", "") for c in _forecaster_columns if c.startswith("Category_")
        )
        raise ValueError(f"Unknown category '{category}'. Known categories: {known}")
    input_row[cat_col] = 1

    pred_log = _forecaster.predict(input_row)[0]
    return float(np.expm1(pred_log))
