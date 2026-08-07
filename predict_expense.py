"""
AI-Based Smart Expense Tracker
Model 2: Expense Prediction Model

Pipeline: Monthly spending history -> Linear Regression -> Next month's predicted expense

This mirrors the "Expense Prediction using Linear Regression" block
in your System Architecture / Proposed System slides.
"""

import pandas as pd
import numpy as np
from sklearn.linear_model import LinearRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error, r2_score

# 1. Load monthly expense history
#    In production, this comes from summing your categorized transactions
#    (from Model 1) grouped by month, pulled from Firebase.
df = pd.read_csv("monthly_expenses.csv")
print("Monthly expense history:")
print(df, "\n")

# 2. Turn "month number in sequence" into the input feature (X),
#    and "total_expense" into what we're predicting (y)
df["month_index"] = range(len(df))
X = df[["month_index"]]
y = df["total_expense"]

# 3. Split into train/test (older months to train, recent months to test)
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, shuffle=False  # shuffle=False keeps time order intact
)

# 4. Train Linear Regression
model = LinearRegression()
model.fit(X_train, y_train)

# 5. Evaluate
preds = model.predict(X_test)
mae = mean_absolute_error(y_test, preds)
r2 = r2_score(y_test, preds)
print(f"Mean Absolute Error: Rs.{mae:.0f}")
print(f"R2 Score: {r2:.2f}  (closer to 1.0 = better fit)\n")

# 6. Predict next month's expense
next_month_index = [[len(df)]]
next_month_pred = model.predict(next_month_index)[0]
print(f"Predicted expense for next month: Rs.{next_month_pred:.0f}")

# 7. Simple budget alert logic (ties into your "Budget Alerts" feature)
user_budget = 17000  # example: user sets a monthly budget limit
if next_month_pred > user_budget:
    over_by = next_month_pred - user_budget
    print(f"BUDGET ALERT: Predicted spending exceeds your Rs.{user_budget} budget by Rs.{over_by:.0f}")
else:
    print(f"You're on track — predicted spending is within your Rs.{user_budget} budget.")
