"""
AI-Based Smart Expense Tracker
Model 3: Anomaly Detection Model

Pipeline: Transaction amounts -> Isolation Forest -> Flag unusual transactions

This mirrors the "Anomaly Detection" feature from your Research Gap /
Literature Review (Paper 5), and supports the Human-in-the-Loop feature
in your Proposed System.
"""

import pandas as pd
from sklearn.ensemble import IsolationForest

# 1. Load the same transaction dataset used for categorization
df = pd.read_csv("sample_transactions_large.csv")
print(f"Loaded {len(df)} transactions\n")

# 2. Train an Isolation Forest on the amount column
#    contamination=0.03 means we expect roughly 3% of transactions to be unusual
model = IsolationForest(contamination=0.03, random_state=42)
df["anomaly_flag"] = model.fit_predict(df[["amount"]])

# IsolationForest returns -1 for anomalies, 1 for normal transactions
df["status"] = df["anomaly_flag"].apply(lambda x: "UNUSUAL" if x == -1 else "normal")

# 3. Show flagged transactions
anomalies = df[df["status"] == "UNUSUAL"].sort_values("amount", ascending=False)
print(f"Flagged {len(anomalies)} unusual transactions out of {len(df)}:\n")
print(anomalies[["merchant_text", "amount", "category"]].to_string(index=False))

# 4. Save results (in production, these get pushed to the app as alerts
#    for the user to confirm/dismiss -- this is the Human-in-the-Loop step)
df.to_csv("transactions_with_anomaly_flags.csv", index=False)
print("\nSaved full results -> transactions_with_anomaly_flags.csv")

# 5. Demo: check a few new transactions against the trained model
new_transactions = pd.DataFrame({
    "amount": [300, 450, 25000, 180, 60000]
})
new_transactions["flag"] = model.predict(new_transactions[["amount"]])
new_transactions["status"] = new_transactions["flag"].apply(lambda x: "UNUSUAL - review this" if x == -1 else "normal")
print("\n--- Checking new transactions ---")
print(new_transactions[["amount", "status"]].to_string(index=False))
