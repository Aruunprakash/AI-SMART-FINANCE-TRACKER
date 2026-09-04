"""
Shared path resolution for the training scripts.

Every script previously used bare filenames (pd.read_csv("sample_transactions.csv"))
while the data actually lives under database/, so the commands documented in
model/README.md all failed. Resolving from __file__ means the scripts work no
matter which directory you run them from.
"""

from pathlib import Path

# model/training/paths.py -> model/training -> model -> <repo root>
ROOT = Path(__file__).resolve().parents[2]

DATABASE = ROOT / "database"
RAW = DATABASE / "raw"
PROCESSED = DATABASE / "processed"
UPI = DATABASE / "upi_transactions"

ARTIFACTS = ROOT / "model" / "artifacts"
EVALUATION = ROOT / "model" / "evaluation"

# Created on import so scripts can always write their output.
ARTIFACTS.mkdir(parents=True, exist_ok=True)
EVALUATION.mkdir(parents=True, exist_ok=True)
PROCESSED.mkdir(parents=True, exist_ok=True)


def require(path: Path) -> Path:
    """Fail loudly and usefully if a dataset is missing (e.g. git lfs not pulled)."""
    if not path.exists():
        raise FileNotFoundError(
            f"Dataset not found: {path}\n"
            f"If this repo was just cloned, the CSVs are stored in Git LFS -- "
            f"run 'git lfs pull' from {ROOT}."
        )
    return path
