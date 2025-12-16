"""
Tests for Risk Engine Routes
"""

import pytest
from fastapi.testclient import TestClient

from hafnium_risk.main import app


@pytest.fixture
def client():
    """Create test client."""
    return TestClient(app)


class TestHealthEndpoints:
    """Tests for health check endpoints."""
    
    def test_health_check(self, client):
        """Test health endpoint returns healthy status."""
        response = client.get("/health")
        assert response.status_code == 200
        assert response.json() == {"status": "healthy"}
    
    def test_ready_check(self, client):
        """Test readiness endpoint returns ready status."""
        response = client.get("/ready")
        assert response.status_code == 200
        assert response.json() == {"status": "ready"}


class TestRiskScoreEndpoints:
    """Tests for risk score endpoints."""
    
    def test_compute_risk_score(self, client):
        """Test computing a risk score."""
        request_body = {
            "entity_type": "customer",
            "entity_id": "cust_12345",
            "context": {
                "use_case": "transaction_approval",
                "amount": 1000.0,
                "currency": "USD",
            },
        }
        
        response = client.post("/api/v1/risk/score", json=request_body)
        
        assert response.status_code == 200
        data = response.json()
        
        assert "score" in data
        assert 0 <= data["score"] <= 1
        assert "risk_level" in data
        assert data["risk_level"] in ["LOW", "MEDIUM", "HIGH", "CRITICAL"]
        assert "reasons" in data
        assert "model_version" in data
        assert "computed_at" in data
    
    def test_compute_risk_score_with_features(self, client):
        """Test computing risk score with pre-computed features."""
        request_body = {
            "entity_type": "transaction",
            "entity_id": "txn_67890",
            "features": {
                "txn_count_24h": 5,
                "txn_sum_24h": 2500.0,
                "days_since_onboarding": 365,
            },
        }
        
        response = client.post("/api/v1/risk/score", json=request_body)
        
        assert response.status_code == 200
        data = response.json()
        assert "score" in data
    
    def test_compute_risk_score_invalid_entity_type(self, client):
        """Test error handling for invalid entity type."""
        request_body = {
            "entity_type": "invalid",
            "entity_id": "test_123",
        }
        
        response = client.post("/api/v1/risk/score", json=request_body)
        
        # Should still work but may produce different results
        # depending on implementation
        assert response.status_code in [200, 422]
    
    def test_get_risk_score_cached(self, client):
        """Test retrieving a cached risk score."""
        response = client.get("/api/v1/risk/score/customer/cust_12345")
        
        # May return 404 if not cached
        assert response.status_code in [200, 404]
    
    def test_batch_risk_score(self, client):
        """Test batch risk scoring."""
        request_body = {
            "requests": [
                {"entity_type": "customer", "entity_id": "cust_001"},
                {"entity_type": "customer", "entity_id": "cust_002"},
                {"entity_type": "customer", "entity_id": "cust_003"},
            ],
        }
        
        response = client.post("/api/v1/risk/score/batch", json=request_body)
        
        assert response.status_code == 200
        data = response.json()
        assert "results" in data
        assert len(data["results"]) == 3


class TestModelEndpoints:
    """Tests for model management endpoints."""
    
    def test_list_models(self, client):
        """Test listing available models."""
        response = client.get("/api/v1/risk/models")
        
        assert response.status_code == 200
        data = response.json()
        assert isinstance(data, list)
        
        if len(data) > 0:
            model = data[0]
            assert "model_id" in model
            assert "model_name" in model
            assert "version" in model
    
    def test_get_model_card(self, client):
        """Test getting model card."""
        response = client.get("/api/v1/risk/models/unified-risk/card")
        
        assert response.status_code == 200
        data = response.json()
        
        assert "model_id" in data
        assert "name" in data
        assert "description" in data
        assert "intended_use" in data
        assert "limitations" in data
        assert "metrics" in data
    
    def test_get_model_card_not_found(self, client):
        """Test getting model card for non-existent model."""
        response = client.get("/api/v1/risk/models/nonexistent/card")
        
        assert response.status_code == 404
