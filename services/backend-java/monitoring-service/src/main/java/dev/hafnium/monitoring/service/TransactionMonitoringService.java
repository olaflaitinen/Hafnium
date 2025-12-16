package dev.hafnium.monitoring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hafnium.common.kafka.EventPublisher;
import dev.hafnium.common.kafka.Topics;
import dev.hafnium.common.security.TenantContext;
import dev.hafnium.monitoring.domain.Alert;
import dev.hafnium.monitoring.domain.Transaction;
import dev.hafnium.monitoring.dto.TransactionRequest;
import dev.hafnium.monitoring.dto.TransactionResponse;
import dev.hafnium.monitoring.repository.AlertRepository;
import dev.hafnium.monitoring.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transaction monitoring service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionMonitoringService {

    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${hafnium.monitoring.alert-threshold-high:0.8}")
    private double highThreshold;

    @Value("${hafnium.monitoring.alert-threshold-critical:0.95}")
    private double criticalThreshold;

    /**
     * Ingests and monitors a transaction.
     */
    @Transactional
    public TransactionResponse ingestTransaction(TransactionRequest request) {
        UUID tenantId = TenantContext.requireTenantId();

        Transaction transaction = Transaction.builder()
                .tenantId(tenantId)
                .customerId(UUID.fromString(request.customerId()))
                .externalId(request.externalId())
                .transactionType(Transaction.TransactionType.valueOf(request.transactionType().toUpperCase()))
                .direction(Transaction.Direction.valueOf(request.direction().toUpperCase()))
                .amount(request.amount())
                .currency(request.currency())
                .counterpartyName(request.counterpartyName())
                .counterpartyAccount(request.counterpartyAccount())
                .counterpartyCountry(request.counterpartyCountry())
                .channel(request.channel())
                .transactionTimestamp(request.transactionTimestamp() != null
                        ? request.transactionTimestamp()
                        : Instant.now())
                .build();

        transaction = transactionRepository.save(transaction);

        log.info("Transaction ingested: id={}, externalId={}, amount={}",
                transaction.getId(), transaction.getExternalId(), transaction.getAmount());

        // Publish ingestion event
        eventPublisher.publish(
                Topics.TXN_INGESTED,
                "txn.ingested",
                "1.0.0",
                transaction.getId().toString(),
                transaction);

        // Run monitoring rules
        List<Alert> alerts = runMonitoringRules(transaction);

        return new TransactionResponse(
                transaction.getId(),
                transaction.getExternalId(),
                transaction.getRiskScore() != null ? transaction.getRiskScore().doubleValue() : null,
                alerts.size());
    }

    private List<Alert> runMonitoringRules(Transaction transaction) {
        List<Alert> alerts = new ArrayList<>();

        // Rule 1: High value transaction
        if (transaction.getAmount().compareTo(BigDecimal.valueOf(10000)) > 0) {
            Alert alert = createAlert(transaction, Alert.AlertType.TRANSACTION_MONITORING,
                    Alert.Severity.HIGH, "HIGH_VALUE", 0.7, "Transaction amount exceeds threshold");
            alerts.add(alert);
        }

        // Rule 2: High-risk country
        List<String> highRiskCountries = List.of("IR", "KP", "SY", "CU");
        if (transaction.getCounterpartyCountry() != null
                && highRiskCountries.contains(transaction.getCounterpartyCountry().toUpperCase())) {
            Alert alert = createAlert(transaction, Alert.AlertType.COUNTRY_RISK,
                    Alert.Severity.CRITICAL, "HIGH_RISK_COUNTRY", 0.95, "Transaction involves high-risk country");
            alerts.add(alert);
        }

        // Publish alerts
        for (Alert alert : alerts) {
            eventPublisher.publish(
                    Topics.ALERT_RAISED,
                    "alert.raised",
                    "1.0.0",
                    alert.getId().toString(),
                    alert);
        }

        return alerts;
    }

    private Alert createAlert(Transaction transaction, Alert.AlertType type,
            Alert.Severity severity, String ruleId, double score, String reason) {

        String reasonsJson;
        try {
            reasonsJson = objectMapper.writeValueAsString(List.of(reason));
        } catch (JsonProcessingException e) {
            reasonsJson = "[]";
        }

        Alert alert = Alert.builder()
                .tenantId(transaction.getTenantId())
                .transaction(transaction)
                .customerId(transaction.getCustomerId())
                .alertType(type)
                .severity(severity)
                .ruleId(ruleId)
                .score(BigDecimal.valueOf(score))
                .reasons(reasonsJson)
                .status(Alert.AlertStatus.OPEN)
                .build();

        return alertRepository.save(alert);
    }
}
