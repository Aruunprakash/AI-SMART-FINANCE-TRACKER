"""
AI-Based Smart Expense Tracker
Model 1: Expense Categorization Model

Pipeline: Merchant/transaction text -> TF-IDF vectorization -> Classifier -> Category

This mirrors the "Machine Learning" block in your System Architecture diagram,
sitting right after Regex + Text Extraction.
"""

import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.pipeline import Pipeline
from sklearn.metrics import classification_report, accuracy_score
import joblib

# 1. Read file and explicitly split columns by comma
df = pd.read_csv("financial_transaction_test.csv", header=None)

# Column 0 is the description/merchant text, Column 1 is the category
df['merchant_text'] = df[0].str.strip()
df['category'] = df[1].str.strip()

# 2. Define features (X) and target (y)
X = df["merchant_text"]
y = df["category"]

# 3. Split data for training and testing (stratify removed to prevent rare category crashes)
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

# 4. Build and train the text classification pipeline
model_pipeline = Pipeline([
    ('tfidf', TfidfVectorizer(stop_words='english', ngram_range=(1, 2))),
    ('classifier', MultinomialNB())
])

print("Training model...")
model_pipeline.fit(X_train, y_train)

# 5. Evaluate and save the trained model
y_pred = model_pipeline.predict(X_test)
print(f"Accuracy: {accuracy_score(y_test, y_pred):.4f}")
print(classification_report(y_test, y_pred))

joblib.dump(model_pipeline, "transaction_categorizer.pkl")
print("Model trained and successfully saved as 'transaction_categorizer.pkl'!")