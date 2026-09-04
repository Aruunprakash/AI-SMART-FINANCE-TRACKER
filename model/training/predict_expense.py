"""
AI-Based Smart Expense Tracker
Model 2: Monthly Expense Forecaster

Pipeline: household transactions -> monthly totals per category
          -> one-hot(Month, Category) -> RandomForest -> log(amount)

Output is written to model/artifacts/ and model/evaluation/.
"""

import json

import numpy as np
import pandas as pd
import joblib
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import KFold, cross_val_predict, cross_val_score
from sklearn.metrics import mean_absolute_error, mean_squared_error

from paths import ARTIFACTS, EVALUATION, RAW, require

print("=" * 60)
print("   AI-SMART FINANCE TRACKER - EXPENSE PREDICTION ENGINE   ")
print("=" * 60)

# 1. Load dataset
dataset_file = require(RAW / "Daily Household Transactions.csv")
df = pd.read_csv(dataset_file)

# 2. Filter expenses & clean dates
if 'Income/Expense' in df.columns:
    df = df[df['Income/Expense'] == 'Expense'].copy()

df['Date'] = pd.to_datetime(df['Date'], dayfirst=True, errors='coerce')
df = df.dropna(subset=['Date'])

# 3. Aggregate monthly spending per category
cat_monthly = df.groupby([df['Date'].dt.to_period('M'), 'Category'])['Amount'].sum().reset_index()
cat_monthly['Month'] = cat_monthly['Date'].dt.month

# 4. Feature encoding
X_raw = pd.get_dummies(cat_monthly[['Month', 'Category']], columns=['Category'], drop_first=False)
y_log = np.log1p(cat_monthly['Amount'])
feature_cols = X_raw.columns.tolist()

# 5. Train model
model = RandomForestRegressor(
    n_estimators=150,
    max_depth=5,
    min_samples_split=3,
    random_state=42
)
model.fit(X_raw, y_log)

# 6. Evaluate -- out-of-fold predictions only.
#
#    The earlier version computed MAE from model.predict(X_raw), i.e. on the
#    same rows the model was fitted on, which understates the error. Since MAE
#    is the headline rupee figure in the report, it has to come from folds that
#    did not see the row. cross_val_predict does exactly that.
kf = KFold(n_splits=5, shuffle=True, random_state=42)
r2_scores = cross_val_score(model, X_raw, y_log, cv=kf, scoring='r2')

y_oof_actual = np.expm1(cross_val_predict(model, X_raw, y_log, cv=kf))
y_true = cat_monthly['Amount']

mae = mean_absolute_error(y_true, y_oof_actual)
rmse = float(np.sqrt(mean_squared_error(y_true, y_oof_actual)))
r2 = float(np.mean(r2_scores))

print("\n--- MODEL ACCURACY METRICS (5-fold cross-validated) ---")
print(f"Samples (month x category) : {len(cat_monthly)}")
print(f"R2 Score                   : {r2:.4f} ({r2 * 100:.1f}% variance explained)")
print(f"MAE                        : Rs {mae:.2f}")
print(f"RMSE                       : Rs {rmse:.2f}")

# 7. Save model + metrics
joblib.dump(model, ARTIFACTS / "final_expense_model.pkl")
with open(ARTIFACTS / "final_expense_model_columns.json", "w") as f:
    json.dump(feature_cols, f)

metrics = {
    "model": "RandomForestRegressor",
    "target": "log1p(monthly amount per category)",
    "samples": int(len(cat_monthly)),
    "cv_folds": 5,
    "r2": round(r2, 4),
    "mae_rupees": round(float(mae), 2),
    "rmse_rupees": round(rmse, 2),
    "note": "MAE and RMSE are out-of-fold (cross_val_predict), not in-sample.",
}
with open(EVALUATION / "forecaster_metrics.json", "w") as f:
    json.dump(metrics, f, indent=2)

print(f"\nModel exported   -> {ARTIFACTS / 'final_expense_model.pkl'}")
print(f"Metrics exported -> {EVALUATION / 'forecaster_metrics.json'}")

# =========================================================
# 8. Live prediction demo
# =========================================================
print("\n" + "=" * 60)
print("         UPCOMING MONTHLY EXPENSE FORECASTS        ")
print("=" * 60)

target_month = 11
categories_to_predict = df['Category'].value_counts().head(5).index.tolist()

print(f"\n[AI forecast for month #{target_month}]:\n")

for cat in categories_to_predict:
    input_data = pd.DataFrame(0, index=[0], columns=feature_cols)
    input_data['Month'] = target_month

    cat_col = f"Category_{cat}"
    if cat_col in input_data.columns:
        input_data[cat_col] = 1

    predicted_amount = np.expm1(model.predict(input_data)[0])
    print(f"  {cat:<20} : Rs {predicted_amount:10.2f}")

print("\n" + "=" * 60)
