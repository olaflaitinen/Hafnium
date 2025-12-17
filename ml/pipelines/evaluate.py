"""
Hafnium ML Evaluation Pipeline

This module implements model evaluation with metrics and reporting.
"""

import argparse
import json
import logging
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Any

import numpy as np
import pandas as pd
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    confusion_matrix,
    precision_recall_curve,
    roc_auc_score,
    roc_curve,
)

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)


@dataclass
class EvaluationReport:
    """Evaluation report for a model."""
    model_id: str
    evaluation_date: str
    dataset: str
    metrics: dict[str, float]
    confusion_matrix: list[list[int]]
    classification_report: dict[str, Any]
    threshold_analysis: dict[str, list[float]]
    
    def to_dict(self) -> dict[str, Any]:
        return {
            "model_id": self.model_id,
            "evaluation_date": self.evaluation_date,
            "dataset": self.dataset,
            "metrics": self.metrics,
            "confusion_matrix": self.confusion_matrix,
            "classification_report": self.classification_report,
            "threshold_analysis": self.threshold_analysis,
        }


class EvaluationPipeline:
    """Evaluation pipeline for risk scoring model."""
    
    def __init__(self, model_path: Path):
        self.model_path = model_path
        self.model = None
        self.scaler = None
        self._load_model()
    
    def _load_model(self):
        """Load trained model."""
        import pickle
        
        model_file = self.model_path
        if model_file.is_dir():
            model_file = model_file / "latest"
            if model_file.is_symlink():
                model_file = model_file.parent / model_file.resolve().name
        
        logger.info(f"Loading model from {model_file}")
        with open(model_file, "rb") as f:
            artifacts = pickle.load(f)
        
        self.model = artifacts["model"]
        self.scaler = artifacts["scaler"]
    
    def evaluate(self, X: np.ndarray, y: np.ndarray) -> EvaluationReport:
        """Evaluate model on test data."""
        logger.info("Running evaluation")
        
        X_scaled = self.scaler.transform(X)
        y_pred = self.model.predict(X_scaled)
        y_proba = self.model.predict_proba(X_scaled)[:, 1]
        
        metrics = {
            "accuracy": float(accuracy_score(y, y_pred)),
            "roc_auc": float(roc_auc_score(y, y_proba)),
        }
        
        cm = confusion_matrix(y, y_pred).tolist()
        
        report = classification_report(y, y_pred, output_dict=True)
        
        fpr, tpr, thresholds = roc_curve(y, y_proba)
        precision, recall, pr_thresholds = precision_recall_curve(y, y_proba)
        
        threshold_analysis = {
            "roc_fpr": fpr.tolist()[:100],
            "roc_tpr": tpr.tolist()[:100],
            "precision": precision.tolist()[:100],
            "recall": recall.tolist()[:100],
        }
        
        model_id = self.model_path.stem if self.model_path.is_file() else "latest"
        
        return EvaluationReport(
            model_id=model_id,
            evaluation_date=datetime.now().isoformat(),
            dataset="test",
            metrics=metrics,
            confusion_matrix=cm,
            classification_report=report,
            threshold_analysis=threshold_analysis,
        )
    
    def generate_report(self, report: EvaluationReport, output_path: Path):
        """Generate evaluation report files."""
        output_path.mkdir(parents=True, exist_ok=True)
        
        json_file = output_path / f"eval_{report.model_id}_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
        with open(json_file, "w") as f:
            json.dump(report.to_dict(), f, indent=2)
        
        md_content = f"""# Model Evaluation Report

**Model ID**: {report.model_id}  
**Evaluation Date**: {report.evaluation_date}  
**Dataset**: {report.dataset}

## Summary Metrics

| Metric | Value |
|--------|-------|
| Accuracy | {report.metrics['accuracy']:.4f} |
| ROC-AUC | {report.metrics['roc_auc']:.4f} |

## Confusion Matrix

|  | Predicted Negative | Predicted Positive |
|--|--------------------|--------------------|
| Actual Negative | {report.confusion_matrix[0][0]} | {report.confusion_matrix[0][1]} |
| Actual Positive | {report.confusion_matrix[1][0]} | {report.confusion_matrix[1][1]} |

## Classification Report

```
Precision (class 1): {report.classification_report.get('1', {}).get('precision', 'N/A')}
Recall (class 1): {report.classification_report.get('1', {}).get('recall', 'N/A')}
F1-Score (class 1): {report.classification_report.get('1', {}).get('f1-score', 'N/A')}
```

## Approval Status

- [ ] Model approved for staging
- [ ] Model approved for production
"""
        
        md_file = output_path / f"eval_{report.model_id}.md"
        with open(md_file, "w") as f:
            f.write(md_content)
        
        logger.info(f"Report saved: {json_file}")
        logger.info(f"Markdown report: {md_file}")


def main():
    parser = argparse.ArgumentParser(description="Hafnium ML Evaluation Pipeline")
    parser.add_argument("--model", type=Path, required=True)
    parser.add_argument("--data", type=Path, required=True)
    parser.add_argument("--output", type=Path, default=Path("reports/"))
    args = parser.parse_args()
    
    pipeline = EvaluationPipeline(args.model)
    
    if args.data.exists():
        if args.data.suffix == ".parquet":
            df = pd.read_parquet(args.data)
        else:
            df = pd.read_csv(args.data)
        
        feature_cols = [
            "transaction_count_30d",
            "avg_amount_30d",
            "max_amount_30d",
            "unique_counterparties",
            "account_age_days",
            "country_risk_score",
        ]
        
        X = df[feature_cols].fillna(0).values
        y = df["is_suspicious"].values
    else:
        logger.warning(f"Data file not found: {args.data}. Using dummy data.")
        X = np.random.randn(500, 6)
        y = (X[:, 0] + X[:, 1] > 0).astype(int)
    
    report = pipeline.evaluate(X, y)
    pipeline.generate_report(report, args.output)
    
    logger.info(f"Evaluation complete. ROC-AUC: {report.metrics['roc_auc']:.4f}")


if __name__ == "__main__":
    main()
