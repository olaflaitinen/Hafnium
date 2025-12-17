"""
Hafnium ML Pipeline Tests
"""

import numpy as np
import pytest


class TestTrainingPipeline:
    """Tests for training pipeline."""
    
    def test_model_metadata_creation(self):
        """Test that model metadata is created correctly."""
        from pipelines.train import ModelMetadata
        
        metadata = ModelMetadata(
            model_id="test-model",
            model_version="20240115_120000",
            training_date="2024-01-15T12:00:00",
            code_commit="abc12345",
            data_version="v1",
            feature_schema_version="v1",
            metrics={"accuracy": 0.95},
            parameters={"n_estimators": 100},
        )
        
        data = metadata.to_dict()
        
        assert data["model_id"] == "test-model"
        assert data["metrics"]["accuracy"] == 0.95
        assert data["parameters"]["n_estimators"] == 100
    
    def test_training_pipeline_initialization(self):
        """Test training pipeline can be initialized."""
        from pipelines.train import TrainingPipeline
        
        config = {"model_params": {"n_estimators": 10}}
        pipeline = TrainingPipeline(config)
        
        assert pipeline.model is None
        assert pipeline.config == config
    
    def test_training_with_dummy_data(self):
        """Test training with synthetic data."""
        from pipelines.train import TrainingPipeline
        
        pipeline = TrainingPipeline({})
        
        X = np.random.randn(100, 6)
        y = (X[:, 0] > 0).astype(int)
        
        metrics = pipeline.train(X, y)
        
        assert "train_accuracy" in metrics
        assert "val_accuracy" in metrics
        assert metrics["train_accuracy"] > 0.5


class TestEvaluationPipeline:
    """Tests for evaluation pipeline."""
    
    def test_evaluation_report_creation(self):
        """Test evaluation report structure."""
        from pipelines.evaluate import EvaluationReport
        
        report = EvaluationReport(
            model_id="test-model",
            evaluation_date="2024-01-15T12:00:00",
            dataset="test",
            metrics={"accuracy": 0.9, "roc_auc": 0.85},
            confusion_matrix=[[50, 10], [5, 35]],
            classification_report={},
            threshold_analysis={},
        )
        
        data = report.to_dict()
        
        assert data["metrics"]["roc_auc"] == 0.85
        assert data["confusion_matrix"][0][0] == 50


class TestDriftDetection:
    """Tests for drift detection."""
    
    def test_ks_drift_no_drift(self):
        """Test KS drift detection with same distribution."""
        from monitoring.drift_detection import DriftDetector
        
        np.random.seed(42)
        reference = np.random.randn(1000, 3)
        current = np.random.randn(1000, 3)
        
        detector = DriftDetector(reference, ["f1", "f2", "f3"])
        results = detector.detect_ks_drift(current, threshold=0.01)
        
        drifted = [r for r in results if r.is_drifted]
        assert len(drifted) <= 1
    
    def test_ks_drift_with_drift(self):
        """Test KS drift detection with shifted distribution."""
        from monitoring.drift_detection import DriftDetector
        
        np.random.seed(42)
        reference = np.random.randn(1000, 3)
        current = np.random.randn(1000, 3) + 5
        
        detector = DriftDetector(reference, ["f1", "f2", "f3"])
        results = detector.detect_ks_drift(current)
        
        drifted = [r for r in results if r.is_drifted]
        assert len(drifted) == 3
    
    def test_psi_calculation(self):
        """Test PSI drift detection."""
        from monitoring.drift_detection import DriftDetector
        
        np.random.seed(42)
        reference = np.random.randn(1000, 2)
        current = np.random.randn(1000, 2)
        
        detector = DriftDetector(reference, ["f1", "f2"])
        results = detector.detect_psi(current)
        
        assert len(results) == 2
        for r in results:
            assert r.statistic >= 0


class TestInferenceServer:
    """Tests for inference server."""
    
    def test_prediction_request_validation(self):
        """Test prediction request schema."""
        from inference.server import PredictionRequest
        
        request = PredictionRequest(
            entity_id="test-123",
            features={
                "transaction_count_30d": 10.0,
                "avg_amount_30d": 500.0,
            }
        )
        
        assert request.entity_id == "test-123"
        assert request.features["transaction_count_30d"] == 10.0
    
    def test_prediction_response_schema(self):
        """Test prediction response schema."""
        from inference.server import PredictionResponse
        
        response = PredictionResponse(
            entity_id="test-123",
            risk_score=0.75,
            risk_level="high",
            model_id="model-v1",
            inference_time_ms=5.5,
        )
        
        assert response.risk_level == "high"
        assert response.inference_time_ms == 5.5
