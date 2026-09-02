import random
import pandas as pd
import joblib
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report, accuracy_score
from sklearn.pipeline import Pipeline

random.seed(42)

# =====================================================================
# STEP 1: Generate synthetic data to augment the small real dataset
# =====================================================================
CITIES = ["Bangalore", "Delhi", "Mumbai", "Hyderabad", "Chennai", "Pune",
          "Kolkata", "Ahmedabad", "Jaipur", "Kochi", "Noida", "Gurgaon"]
SUFFIXES = ["Order", "Payment", "Purchase", "Bill", "Transaction", "Store",
            "Outlet", "Delivery", "Booking", ""]

CATEGORY_BRANDS = {
    "Food": ["Swiggy", "Zomato", "McDonalds", "Domino's Pizza", "Starbucks",
             "KFC", "Burger King", "Pizza Hut", "Cafe Coffee Day",
             "Subway", "Behrouz Biryani", "Faasos", "Barbeque Nation",
             "Chaayos", "Wow Momo", "Haldiram's"],
    "Groceries": ["BigBasket", "DMart", "Reliance Fresh", "More Supermarket",
                  "Nature's Basket", "Metro Cash and Carry", "Blinkit",
                  "Zepto", "Star Bazaar", "Spencer's Retail", "JioMart",
                  "Grofers"],
    "Travel": ["Uber", "Ola Cabs", "IRCTC Railways", "IndiGo Airlines",
               "Rapido Bike", "Vistara Airlines", "SpiceJet", "Ola Auto",
               "RedBus", "MakeMyTrip", "Yatra", "GoAir", "Air India"],
    "Shopping": ["Amazon", "Flipkart", "Myntra", "Ajio", "Decathlon Sports",
                 "Croma Electronics", "Nykaa Beauty", "Tata Cliq",
                 "Reliance Digital", "Lifestyle Stores", "H&M", "Snapdeal"],
    "Bills": ["Electricity Board", "Airtel Postpaid", "Jio Recharge",
              "ACT Broadband", "Water Board", "Tata Sky", "Vodafone Idea",
              "BSNL", "Gas Pipeline", "Urban Company Service",
              "Hathway Broadband", "MTNL"],
    "Healthcare": ["Apollo Pharmacy", "Practo Consultation", "Medplus Store",
                   "Cult Fit Membership", "1mg Pharmacy", "Netmeds",
                   "Fortis Hospital", "Max Healthcare", "PharmEasy",
                   "Cure Fit"],
    "Entertainment": ["PVR Cinemas", "BookMyShow Tickets", "Netflix",
                       "Spotify Premium", "INOX Movies", "Hotstar",
                       "Amazon Prime Video", "Cinepolis", "SonyLIV",
                       "ZEE5 Subscription"],
    "Investment": ["HDFC Mutual Fund SIP", "Zerodha Trading", "LIC Premium",
                   "Groww Investment", "ICICI Direct", "Upstox Trading",
                   "SBI Mutual Fund", "Paytm Money", "Angel One",
                   "PPF Deposit"],
    "Rent": ["Rent Payment Landlord", "House Rent NEFT", "Flat Rent Transfer",
             "PG Rent Payment", "Apartment Rent", "Society Maintenance Rent"],
    "Transfer": ["PhonePe to Friend", "GPay Transfer", "Paytm Wallet Load",
                 "UPI Transfer", "Bank NEFT Transfer", "IMPS Transfer",
                 "Cash Deposit Transfer"],
}

AMOUNT_RANGES = {
    "Food": (80, 900), "Groceries": (300, 3000), "Travel": (60, 6000),
    "Shopping": (400, 6000), "Bills": (150, 2500), "Healthcare": (150, 2000),
    "Entertainment": (100, 900), "Investment": (1000, 10000),
    "Rent": (8000, 25000), "Transfer": (100, 5000),
}

N_PER_CATEGORY = 60


def make_text(brand):
    variant = random.random()
    if variant < 0.35:
        return f"{brand} {random.choice(CITIES)}"
    elif variant < 0.6:
        suf = random.choice(SUFFIXES)
        return f"{brand} {suf}".strip()
    elif variant < 0.8:
        return f"{brand} #{random.randint(1000, 99999)}"
    else:
        return brand


synthetic_rows = []
for category, brands in CATEGORY_BRANDS.items():
    lo, hi = AMOUNT_RANGES[category]
    for _ in range(N_PER_CATEGORY):
        brand = random.choice(brands)
        synthetic_rows.append({
            "merchant_text": make_text(brand),
            "amount": random.randint(lo, hi),
            "category": category,
        })
synthetic_df = pd.DataFrame(synthetic_rows)

# =====================================================================
# STEP 2: Combine with the real hand-labeled data
# =====================================================================
real_df = pd.read_csv("sample_transactions.csv")

combined_df = pd.concat([real_df, synthetic_df], ignore_index=True)
combined_df = combined_df.sample(frac=1, random_state=42).reset_index(drop=True)
combined_df.to_csv("sample_transactions_large.csv", index=False)

print(f"Real rows: {len(real_df)} | Synthetic rows: {len(synthetic_df)} | Combined: {len(combined_df)}")
print(combined_df['category'].value_counts(), "\n")

# =====================================================================
# STEP 3: Held-out test set = REAL data only (never trained on)
#   -- the honest way to check generalization, since testing on synthetic
#      rows would inflate the score artificially
# =====================================================================
real_train, real_test = train_test_split(
    real_df, test_size=0.3, random_state=42, stratify=real_df["category"]
)
train_df = pd.concat([real_train, synthetic_df], ignore_index=True)

X_train, y_train = train_df["merchant_text"], train_df["category"]
X_test, y_test = real_test["merchant_text"], real_test["category"]

print(f"Training on {len(train_df)} rows, testing on {len(real_test)} REAL (unseen) rows\n")

# =====================================================================
# STEP 4: Pipelines -- unigrams + min_df=2 instead of bigrams + min_df=1
#   (bigrams on ~54 real rows created more features than samples, the
#    main cause of the overfitting)
# =====================================================================
pipelines = {
    "naive_bayes": Pipeline([
        ("tfidf", TfidfVectorizer(ngram_range=(1, 1), min_df=2, max_df=0.9,
                                   sublinear_tf=True)),
        ("clf", MultinomialNB(alpha=0.5)),
    ]),
    "logistic_regression": Pipeline([
        ("tfidf", TfidfVectorizer(ngram_range=(1, 1), min_df=2, max_df=0.9,
                                   sublinear_tf=True)),
        ("clf", LogisticRegression(max_iter=1000, C=1.0)),
    ]),
    "random_forest": Pipeline([
        ("tfidf", TfidfVectorizer(ngram_range=(1, 1), min_df=2, max_df=0.9,
                                   sublinear_tf=True)),
        ("clf", RandomForestClassifier(n_estimators=200, max_depth=15,
                                        random_state=42)),
    ]),
}

best_model, best_score, best_name = None, 0, None

for name, pipe in pipelines.items():
    cv_scores = cross_val_score(pipe, X_train, y_train, cv=5)
    pipe.fit(X_train, y_train)

    train_acc = accuracy_score(y_train, pipe.predict(X_train))
    test_acc = accuracy_score(y_test, pipe.predict(X_test))

    print(f"=== {name} ===")
    print(f"CV accuracy (5-fold, train set): {cv_scores.mean():.2f} (+/- {cv_scores.std():.2f})")
    print(f"Train accuracy: {train_acc:.2f}")
    print(f"Real-data test accuracy: {test_acc:.2f}")
    print(f"Overfit gap (train - test): {train_acc - test_acc:.2f}")
    print(classification_report(y_test, pipe.predict(X_test), zero_division=0))

    if test_acc >= best_score:
        best_score, best_model, best_name = test_acc, pipe, name

print(f"Best model: {best_name} (real-data test accuracy: {best_score:.2f})")

# =====================================================================
# STEP 5: Save the winning model
# =====================================================================
joblib.dump(best_model, "expense_category_model.joblib")
print("Saved model -> expense_category_model.joblib")

# =====================================================================
# STEP 6: Demo on brand-new, unseen transaction strings
# =====================================================================
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
