#!/bin/bash
set -e

echo "Seeding Demo Data..."

# Base URL
API_URL="http://localhost:8087/api/v1"

# 1. Create Customer
echo "Creating Customer..."
curl -X POST "$API_URL/customers" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "type": "INDIVIDUAL",
    "riskProfile": "LOW"
  }'

# 2. Create Transaction
echo "Ingesting Transaction..."
curl -X POST "$API_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000.00,
    "currency": "USD",
    "counterpartyId": "CP-123",
    "counterpartyCountry": "US",
    "type": "WIRE_TRANSFER"
  }'

echo "Data seeded successfully."
