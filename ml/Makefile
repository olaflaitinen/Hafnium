.PHONY: help setup lint test train evaluate serve clean

PYTHON := python
PIP := pip

help:
	@echo "Hafnium ML Pipeline Commands"
	@echo ""
	@echo "  setup      Install dependencies"
	@echo "  lint       Run linting"
	@echo "  test       Run tests"
	@echo "  train      Run training pipeline"
	@echo "  evaluate   Run evaluation pipeline"
	@echo "  serve      Start inference server"
	@echo "  clean      Clean artifacts"

setup:
	$(PIP) install -e ".[dev,training,serving]"
	pre-commit install

lint:
	ruff check .
	mypy hafnium_ml/

test:
	pytest tests/ -v --cov=hafnium_ml

train:
	$(PYTHON) -m hafnium_ml.pipelines.train \
		--config training/config/default.yaml \
		--output models/

evaluate:
	$(PYTHON) -m hafnium_ml.pipelines.evaluate \
		--model models/latest \
		--data data/test.parquet \
		--output reports/

serve:
	uvicorn hafnium_ml.inference.server:app --host 0.0.0.0 --port 8000 --reload

clean:
	rm -rf models/*.pkl
	rm -rf reports/*.json
	rm -rf .pytest_cache
	rm -rf __pycache__
	find . -type d -name "__pycache__" -exec rm -rf {} + 2>/dev/null || true
