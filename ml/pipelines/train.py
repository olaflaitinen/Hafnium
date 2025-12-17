"""
Hafnium ML Training Pipeline

This module implements the training pipeline for risk scoring models.
"""

import argparse
import hashlib
import json
import logging
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Any

import numpy as np
import pandas as pd
from sklearn.ensemble import GradientBoostingClassifier
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)


@dataclass
class ModelMetadata:
    """Metadata for a trained model."""
    model_id: str
    model_version: str
    training_date: str
    code_commit: str
    data_version: str
    feature_schema_version: str
    metrics: dict[str, float]
    parameters: dict[str, Any]
    
    def to_dict(self) -> dict[str, Any]:
        return {
            "model_id": self.model_id,
            "model_version": self.model_version,
            "training_date": self.training_date,
            "code_commit": self.code_commit,
            "data_version": self.data_version,
            "feature_schema_version": self.feature_schema_version,
            "metrics": self.metrics,
            "parameters": self.parameters,
        }


class TrainingPipeline:
    """Training pipeline for risk scoring model."""
    
    def __init__(self, config: dict[str, Any]):
        self.config = config
        self.model = None
        self.scaler = StandardScaler()
        
    def load_data(self, data_path: Path) -> pd.DataFrame:
        """Load training data."""
        logger.info(f"Loading data from {data_path}")
        
        if data_path.suffix == ".parquet":
            return pd.read_parquet(data_path)
        elif data_path.suffix == ".csv":
            return pd.read_csv(data_path)
        else:
            raise ValueError(f"Unsupported file format: {data_path.suffix}")
    
    def preprocess(self, df: pd.DataFrame) -> tuple[np.ndarray, np.ndarray]:
        """Preprocess data for training."""
        logger.info("Preprocessing data")
        
        feature_cols = self.config.get("features", [
            "transaction_count_30d",
            "avg_amount_30d",
            "max_amount_30d",
            "unique_counterparties",
            "account_age_days",
            "country_risk_score",
        ])
        
        target_col = self.config.get("target", "is_suspicious")
        
        X = df[feature_cols].fillna(0).values
        y = df[target_col].values
        
        X = self.scaler.fit_transform(X)
        
        return X, y
    
    def train(self, X: np.ndarray, y: np.ndarray) -> dict[str, float]:
        """Train the model."""
        logger.info("Starting model training")
        
        X_train, X_val, y_train, y_val = train_test_split(
            X, y, test_size=0.2, random_state=42, stratify=y
        )
        
        params = self.config.get("model_params", {
            "n_estimators": 100,
            "max_depth": 5,
            "learning_rate": 0.1,
            "min_samples_split": 10,
        })
        
        self.model = GradientBoostingClassifier(**params, random_state=42)
        self.model.fit(X_train, y_train)
        
        train_score = self.model.score(X_train, y_train)
        val_score = self.model.score(X_val, y_val)
        
        logger.info(f"Training accuracy: {train_score:.4f}")
        logger.info(f"Validation accuracy: {val_score:.4f}")
        
        return {
            "train_accuracy": train_score,
            "val_accuracy": val_score,
        }
    
    def save(self, output_path: Path, metrics: dict[str, float]) -> ModelMetadata:
        """Save model and metadata."""
        import pickle
        
        output_path.mkdir(parents=True, exist_ok=True)
        
        model_version = datetime.now().strftime("%Y%m%d_%H%M%S")
        model_id = f"risk-model-{model_version}"
        
        model_file = output_path / f"{model_id}.pkl"
        with open(model_file, "wb") as f:
            pickle.dump({"model": self.model, "scaler": self.scaler}, f)
        
        with open(model_file, "rb") as f:
            model_hash = hashlib.sha256(f.read()).hexdigest()[:16]
        
        metadata = ModelMetadata(
            model_id=model_id,
            model_version=model_version,
            training_date=datetime.now().isoformat(),
            code_commit=self._get_git_commit(),
            data_version="v1",
            feature_schema_version="v1",
            metrics=metrics,
            parameters=self.config.get("model_params", {}),
        )
        
        metadata_file = output_path / f"{model_id}_metadata.json"
        with open(metadata_file, "w") as f:
            json.dump(metadata.to_dict(), f, indent=2)
        
        latest_link = output_path / "latest"
        if latest_link.exists():
            latest_link.unlink()
        latest_link.symlink_to(model_file.name)
        
        logger.info(f"Model saved: {model_file}")
        return metadata
    
    def _get_git_commit(self) -> str:
        """Get current git commit hash."""
        try:
            import subprocess
            result = subprocess.run(
                ["git", "rev-parse", "HEAD"],
                capture_output=True,
                text=True,
            )
            return result.stdout.strip()[:8]
        except Exception:
            return "unknown"


def main():
    parser = argparse.ArgumentParser(description="Hafnium ML Training Pipeline")
    parser.add_argument("--config", type=Path, default=Path("training/config/default.yaml"))
    parser.add_argument("--data", type=Path, default=Path("data/training.parquet"))
    parser.add_argument("--output", type=Path, default=Path("models/"))
    args = parser.parse_args()
    
    config = {}
    if args.config.exists():
        import yaml
        with open(args.config) as f:
            config = yaml.safe_load(f)
    
    pipeline = TrainingPipeline(config)
    
    if args.data.exists():
        df = pipeline.load_data(args.data)
        X, y = pipeline.preprocess(df)
        metrics = pipeline.train(X, y)
        metadata = pipeline.save(args.output, metrics)
        logger.info(f"Training complete: {metadata.model_id}")
    else:
        logger.warning(f"Data file not found: {args.data}. Creating dummy model.")
        X = np.random.randn(1000, 6)
        y = (X[:, 0] + X[:, 1] > 0).astype(int)
        metrics = pipeline.train(X, y)
        metadata = pipeline.save(args.output, metrics)
        logger.info(f"Dummy model created: {metadata.model_id}")


if __name__ == "__main__":
    main()
