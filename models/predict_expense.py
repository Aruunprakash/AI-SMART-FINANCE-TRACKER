import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import KFold, cross_val_score
from sklearn.metrics import mean_absolute_error
import joblib

print("="*60)
print("   AI-SMART FINANCE TRACKER - EXPENSE PREDICTION ENGINE   ")
print("="*60)

# 1. Load Dataset
dataset_file = 'Daily Household Transactions.csv'
df = pd.read_csv(dataset_file)

# 2. Filter Expenses & Clean Dates
if 'Income/Expense' in df.columns:
    df = df[df['Income/Expense'] == 'Expense'].copy()

df['Date'] = pd.to_datetime(df['Date'], dayfirst=True, errors='coerce')
df = df.dropna(subset=['Date'])

# 3. Aggregate Monthly Spending per Category
cat_monthly = df.groupby([df['Date'].dt.to_period('M'), 'Category'])['Amount'].sum().reset_index()
cat_monthly['Month'] = cat_monthly['Date'].dt.month

# 4. Feature Encoding
X_raw = pd.get_dummies(cat_monthly[['Month', 'Category']], columns=['Category'], drop_first=False)
y_log = np.log1p(cat_monthly['Amount'])

# Feature Columns Reference
feature_cols = X_raw.columns.tolist()

# 5. Train Model
model = RandomForestRegressor(
    n_estimators=150,
    max_depth=5,
    min_samples_split=3,
    random_state=42
)
model.fit(X_raw, y_log)

# 6. Evaluate Model
kf = KFold(n_splits=5, shuffle=True, random_state=42)
r2_scores = cross_val_score(model, X_raw, y_log, cv=kf, scoring='r2')

# Calculate Average MAE
y_pred_log = model.predict(X_raw)
y_pred_actual = np.expm1(y_pred_log)
mae = mean_absolute_error(cat_monthly['Amount'], y_pred_actual)

print("\n--- MODEL ACCURACY METRICS ---")
print(f"✔ Cross-Validated R² Score : {np.mean(r2_scores):.4f} ({np.mean(r2_scores)*100:.1f}% Variance Explained)")
print(f"✔ Mean Absolute Error (MAE): ₹{mae:.2f}")

# 7. Save Model
joblib.dump(model, 'final_expense_model.pkl')
print("\n✔ Model successfully exported -> 'final_expense_model.pkl'")

# =========================================================
# 8. LIVE PREDICTION SYSTEM (FOR PRESENTATION DEMO)
# =========================================================
print("\n" + "="*60)
print("         UPCOMING MONTHLY EXPENSE FORECASTS        ")
print("="*60)

target_month = 11  # Next Month (November)
categories_to_predict = df['Category'].value_counts().head(5).index.tolist()

print(f"\n[AI Forecast for Month #{target_month}]:\n")

for cat in categories_to_predict:
    # Build input feature vector
    input_data = pd.DataFrame(0, index=[0], columns=feature_cols)
    input_data['Month'] = target_month
    
    cat_col = f"Category_{cat}"
    if cat_col in input_data.columns:
        input_data[cat_col] = 1
        
    # Predict
    pred_log = model.predict(input_data)[0]
    predicted_amount = np.expm1(pred_log)
    
    print(f" 📌 {cat:<20} : ₹{predicted_amount:10.2f}")

print("\n" + "="*60)
print("  PRESENTATION DEMO READY!   ")
print("="*60)