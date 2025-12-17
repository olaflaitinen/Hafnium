"""
Hafnium ML Drift Detection

Monitors for data and model drift in production.
"""

import logging
from dataclasses import dataclass
from typing import Any

import numpy as np
from scipy import stats

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@dataclass
class DriftResult:
    """Result of drift detection."""
    feature: str
    drift_type: str
    statistic: float
    p_value: float
    threshold: float
    is_drifted: bool


class DriftDetector:
    """Detects data and prediction drift."""
    
    def __init__(self, reference_data: np.ndarray, feature_names: list[str]):
        self.reference_data = reference_data
        self.feature_names = feature_names
        self.reference_stats = self._compute_stats(reference_data)
    
    def _compute_stats(self, data: np.ndarray) -> dict[str, dict[str, float]]:
        """Compute statistics for each feature."""
        stats_dict = {}
        for i, name in enumerate(self.feature_names):
            col = data[:, i]
            stats_dict[name] = {
                "mean": float(np.mean(col)),
                "std": float(np.std(col)),
                "min": float(np.min(col)),
                "max": float(np.max(col)),
                "median": float(np.median(col)),
            }
        return stats_dict
    
    def detect_ks_drift(
        self, 
        current_data: np.ndarray, 
        threshold: float = 0.05
    ) -> list[DriftResult]:
        """Detect drift using Kolmogorov-Smirnov test."""
        results = []
        
        for i, name in enumerate(self.feature_names):
            ref_col = self.reference_data[:, i]
            cur_col = current_data[:, i]
            
            statistic, p_value = stats.ks_2samp(ref_col, cur_col)
            
            is_drifted = p_value < threshold
            
            results.append(DriftResult(
                feature=name,
                drift_type="distribution",
                statistic=float(statistic),
                p_value=float(p_value),
                threshold=threshold,
                is_drifted=is_drifted,
            ))
            
            if is_drifted:
                logger.warning(f"Drift detected in {name}: KS={statistic:.4f}, p={p_value:.4f}")
        
        return results
    
    def detect_psi(
        self, 
        current_data: np.ndarray, 
        n_bins: int = 10,
        threshold: float = 0.2
    ) -> list[DriftResult]:
        """Detect drift using Population Stability Index."""
        results = []
        
        for i, name in enumerate(self.feature_names):
            ref_col = self.reference_data[:, i]
            cur_col = current_data[:, i]
            
            bins = np.linspace(
                min(ref_col.min(), cur_col.min()),
                max(ref_col.max(), cur_col.max()),
                n_bins + 1
            )
            
            ref_hist, _ = np.histogram(ref_col, bins=bins)
            cur_hist, _ = np.histogram(cur_col, bins=bins)
            
            ref_pct = (ref_hist + 1) / (len(ref_col) + n_bins)
            cur_pct = (cur_hist + 1) / (len(cur_col) + n_bins)
            
            psi = np.sum((cur_pct - ref_pct) * np.log(cur_pct / ref_pct))
            
            is_drifted = psi > threshold
            
            results.append(DriftResult(
                feature=name,
                drift_type="psi",
                statistic=float(psi),
                p_value=0.0,
                threshold=threshold,
                is_drifted=is_drifted,
            ))
            
            if is_drifted:
                logger.warning(f"PSI drift in {name}: PSI={psi:.4f}")
        
        return results
    
    def check_prediction_drift(
        self,
        reference_predictions: np.ndarray,
        current_predictions: np.ndarray,
        threshold: float = 0.1
    ) -> DriftResult:
        """Check for prediction distribution drift."""
        ref_mean = np.mean(reference_predictions)
        cur_mean = np.mean(current_predictions)
        
        drift_magnitude = abs(cur_mean - ref_mean)
        is_drifted = drift_magnitude > threshold
        
        if is_drifted:
            logger.warning(f"Prediction drift: ref_mean={ref_mean:.4f}, cur_mean={cur_mean:.4f}")
        
        return DriftResult(
            feature="predictions",
            drift_type="mean_shift",
            statistic=float(drift_magnitude),
            p_value=0.0,
            threshold=threshold,
            is_drifted=is_drifted,
        )


def generate_alert_rules() -> dict[str, Any]:
    """Generate Prometheus alerting rules for drift detection."""
    return {
        "groups": [
            {
                "name": "ml-drift-alerts",
                "rules": [
                    {
                        "alert": "MLDataDriftDetected",
                        "expr": "hafnium_ml_drift_psi > 0.2",
                        "for": "1h",
                        "labels": {"severity": "warning"},
                        "annotations": {
                            "summary": "Data drift detected in ML features",
                            "description": "PSI score exceeded threshold",
                        },
                    },
                    {
                        "alert": "MLPredictionDrift",
                        "expr": "abs(hafnium_ml_prediction_mean - hafnium_ml_prediction_mean offset 7d) > 0.1",
                        "for": "2h",
                        "labels": {"severity": "warning"},
                        "annotations": {
                            "summary": "Prediction distribution has shifted",
                        },
                    },
                    {
                        "alert": "MLInferenceLatencyHigh",
                        "expr": "histogram_quantile(0.99, rate(hafnium_ml_inference_duration_seconds_bucket[5m])) > 0.1",
                        "for": "5m",
                        "labels": {"severity": "critical"},
                        "annotations": {
                            "summary": "ML inference P99 latency exceeds 100ms",
                        },
                    },
                ]
            }
        ]
    }
