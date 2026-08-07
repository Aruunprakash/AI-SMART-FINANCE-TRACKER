"""
AI-Based Smart Expense Tracker
Model 1: Expense Categorization Model

Pipeline: Merchant/transaction text -> TF-IDF vectorization -> Classifier -> Category

This mirrors the "Machine Learning" block in your System Architecture diagram,
sitting right after Regex + Text Extraction.
"""

import pandas as pd
import joblib
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report, accuracy_score
from sklearn.pipeline import Pipeline

# 1. Load data (in production this comes from your regex-parsed notification data
#    stored via Firebase Firestore / Room local DB, exported to CSV)
df = pd.read_csv("sample_transactions_large.csv")
print(f"Loaded {len(df)} labeled transactions across {df['category'].nunique()} categories")
print(df['category'].value_counts(), "\n")

X = df["merchant_text"]
y = df["category"]

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42, stratify=y
)

# 2. Build pipeline: TF-IDF -> Classifier
#    Naive Bayes works well for short text with small datasets like this.
#    RandomForest is included as an alternative/comparison (also used in your Lit Review, Paper 2).
pipelines = {
    "naive_bayes": Pipeline([
        ("tfidf", TfidfVectorizer(ngram_range=(1, 2), min_df=1)),
        ("clf", MultinomialNB())
    ]),
    "random_forest": Pipeline([
        ("tfidf", TfidfVectorizer(ngram_range=(1, 2), min_df=1)),
        ("clf", RandomForestClassifier(n_estimators=200, random_state=42))
    ]),
}

best_model = None
best_score = 0
best_name = None

for name, pipe in pipelines.items():
    pipe.fit(X_train, y_train)
    preds = pipe.predict(X_test)
    acc = accuracy_score(y_test, preds)
    print(f"=== {name} ===")
    print(f"Accuracy: {acc:.2f}")
    print(classification_report(y_test, preds, zero_division=0))
    if acc >= best_score:
        best_score = acc
        best_model = pipe
        best_name = name

print(f"Best model: {best_name} (accuracy: {best_score:.2f})")

# 3. Save the winning model
joblib.dump(best_model, "expense_category_model.joblib")
print("Saved model -> expense_category_model.joblib")

# 4. Demo: classify a few brand-new, unseen transaction strings
sample_new = [
    "Blinkit Grocery Delivery",
    "SBI Life Insurance Premium",
    "Uber Eats Order",
    "Vodafone Idea Recharge",
]
predictions = best_model.predict(sample_new)
print("\n--- Live predictions on unseen text ---")
for text, pred in zip(sample_new, predictions):
    print(f"{text:35s} -> {pred}")
