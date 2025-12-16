"""Feast Feature Store Definitions for Hafnium."""

from datetime import timedelta

from feast import Entity, Feature, FeatureService, FeatureView, Field, FileSource
from feast.types import Float32, Int64, String


# Entities
customer = Entity(
    name="customer",
    join_keys=["customer_id"],
    description="Customer entity",
)

transaction = Entity(
    name="transaction",
    join_keys=["transaction_id"],
    description="Transaction entity",
)

# Data Sources
customer_features_source = FileSource(
    name="customer_features_source",
    path="data/customer_features.parquet",
    timestamp_field="event_timestamp",
)

transaction_features_source = FileSource(
    name="transaction_features_source",
    path="data/transaction_features.parquet",
    timestamp_field="event_timestamp",
)

# Feature Views
customer_profile_fv = FeatureView(
    name="customer_profile",
    entities=[customer],
    ttl=timedelta(days=1),
    schema=[
        Field(name="customer_age_days", dtype=Int64),
        Field(name="kyc_status", dtype=String),
        Field(name="risk_tier", dtype=String),
        Field(name="transaction_count_lifetime", dtype=Int64),
        Field(name="avg_transaction_amount", dtype=Float32),
        Field(name="country_code", dtype=String),
    ],
    source=customer_features_source,
    online=True,
)

transaction_velocity_fv = FeatureView(
    name="transaction_velocity",
    entities=[customer],
    ttl=timedelta(hours=1),
    schema=[
        Field(name="transaction_count_1h", dtype=Int64),
        Field(name="transaction_count_24h", dtype=Int64),
        Field(name="transaction_amount_1h", dtype=Float32),
        Field(name="transaction_amount_24h", dtype=Float32),
        Field(name="unique_counterparties_24h", dtype=Int64),
        Field(name="velocity_ratio", dtype=Float32),
    ],
    source=transaction_features_source,
    online=True,
)

device_signals_fv = FeatureView(
    name="device_signals",
    entities=[customer],
    ttl=timedelta(hours=1),
    schema=[
        Field(name="device_risk_score", dtype=Float32),
        Field(name="session_count_24h", dtype=Int64),
        Field(name="unique_devices_30d", dtype=Int64),
        Field(name="ip_reputation_score", dtype=Float32),
    ],
    source=customer_features_source,
    online=True,
)

network_analysis_fv = FeatureView(
    name="network_analysis",
    entities=[customer],
    ttl=timedelta(days=1),
    schema=[
        Field(name="network_risk_score", dtype=Float32),
        Field(name="connected_high_risk_entities", dtype=Int64),
        Field(name="network_centrality", dtype=Float32),
    ],
    source=customer_features_source,
    online=True,
)

# Feature Services
risk_scoring_fs = FeatureService(
    name="risk_scoring",
    features=[
        customer_profile_fv[["customer_age_days", "risk_tier"]],
        transaction_velocity_fv,
        device_signals_fv[["device_risk_score"]],
        network_analysis_fv[["network_risk_score"]],
    ],
    description="Features for real-time risk scoring",
)

fraud_detection_fs = FeatureService(
    name="fraud_detection",
    features=[
        transaction_velocity_fv,
        device_signals_fv,
        network_analysis_fv,
    ],
    description="Features for fraud detection models",
)
