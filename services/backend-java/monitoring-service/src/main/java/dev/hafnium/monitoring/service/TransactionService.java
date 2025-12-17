package dev.hafnium.monitoring.service;

import dev.hafnium.common.kafka.KafkaEventPublisher;
import dev.hafnium.common.model.event.EventType;
import dev.hafnium.common.security.TenantContext;
import dev.hafnium.monitoring.domain.Alert;
import dev.hafnium.monitoring.domain.Transaction;
import dev.hafnium.monitoring.dto.TransactionRequest;
import dev.hafnium.monitoring.dto.TransactionResponse;
import dev.hafnium.monitoring.engine.RuleEngine;
import dev.hafnium.monitoring.repository.AlertRepository;
import dev.hafnium.monitoring.repository.TransactionRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for transaction ingestion and monitoring.
 *
 * <p>
 * Handles transaction ingestion, rule evaluation, and alert generation.
 */
@Service
@Transactional
public class TransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    private final RuleEngine ruleEngine;
    private final KafkaEventPublisher eventPublisher;

    public TransactionService(
            TransactionRepository transactionRepository,
            AlertRepository alertRepository,
            RuleEngine ruleEngine,
            KafkaEventPublisher eventPublisher) {
        this.transactionRepository = transactionRepository;
        this.alertRepository = alertRepository;
        this.ruleEngine = ruleEngine;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Ingests a new transaction.
     *
     * @param request The transaction request
     * @return The transaction response with any generated alerts
     */
    public TransactionResponse ingestTransaction(TransactionRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        String actorId = TenantContext.requireActorId();

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setTenantId(tenantId);
        transaction.setCustomerId(request.customerId());
        transaction.setExternalTxnId(request.externalTxnId());
        transaction.setAmount(request.amount());
        transaction.setCurrency(request.currency());
        transaction.setTxnType(request.txnType());
        transaction.setTxnTimestamp(request.txnTimestamp());
        transaction.setCounterpartyId(request.counterpartyId());
        transaction.setCounterpartyName(request.counterpartyName());
        transaction.setChannel(request.channel());
        transaction.setGeoData(request.geoData());
        transaction.setMetadata(request.metadata());

        transaction = transactionRepository.save(transaction);

        LOG.info(
                "Ingested transaction {} for tenant {} customer {}",
                transaction.getTxnId(),
                tenantId,
                request.customerId());

        // Emit transaction ingested event
        eventPublisher.publish(
                EventType.TRANSACTION_INGESTED,
                tenantId,
                actorId,
                TenantContext.getOrCreateTraceId(),
                Map.of(
                        "txn_id", transaction.getTxnId(),
                        "customer_id", transaction.getCustomerId() != null ? transaction.getCustomerId() : "",
                        "amount", transaction.getAmount(),
                        "currency", transaction.getCurrency(),
                        "txn_type", transaction.getTxnType().name()));

        // Evaluate rules
        List<Alert> alerts = ruleEngine.evaluate(transaction);

        // Save any generated alerts
        if (!alerts.isEmpty()) {
            alertRepository.saveAll(alerts);

            LOG.info(
                    "Generated {} alerts for transaction {}",
                    alerts.size(),
                    transaction.getTxnId());

            // Emit alert events
            for (Alert alert : alerts) {
                eventPublisher.publish(
                        EventType.ALERT_RAISED,
                        tenantId,
                        actorId,
                        TenantContext.getOrCreateTraceId(),
                        Map.of(
                                "alert_id", alert.getAlertId(),
                                "txn_id", transaction.getTxnId(),
                                "rule_id", alert.getRuleId(),
                                "severity", alert.getSeverity().name(),
                                "score", alert.getScore()));
            }
        }

        return new TransactionResponse(
                transaction.getTxnId(),
                transaction.getExternalTxnId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTxnType().name().toLowerCase(),
                transaction.getRiskScore(),
                alerts.size(),
                transaction.getCreatedAt());
    }
}
