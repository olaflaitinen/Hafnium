package dev.hafnium.monitoring.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Transaction domain entity.
 *
 * <p>
 * Represents a financial transaction ingested for monitoring and risk
 * assessment.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "txn_id")
    private UUID txnId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "external_txn_id")
    private String externalTxnId;

    @Column(name = "amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "txn_type", nullable = false)
    private TransactionType txnType;

    @Column(name = "txn_timestamp", nullable = false)
    private Instant txnTimestamp;

    @Column(name = "counterparty_id")
    private String counterpartyId;

    @Column(name = "counterparty_name")
    private String counterpartyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel")
    private Channel channel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "geo_data", columnDefinition = "jsonb")
    private Map<String, Object> geoData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "risk_score", precision = 5, scale = 4)
    private BigDecimal riskScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "risk_factors", columnDefinition = "jsonb")
    private Map<String, Object> riskFactors;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Transaction() {
        this.txnId = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

    // Getters and setters

    public UUID getTxnId() {
        return txnId;
    }

    public void setTxnId(UUID txnId) {
        this.txnId = txnId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getExternalTxnId() {
        return externalTxnId;
    }

    public void setExternalTxnId(String externalTxnId) {
        this.externalTxnId = externalTxnId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public TransactionType getTxnType() {
        return txnType;
    }

    public void setTxnType(TransactionType txnType) {
        this.txnType = txnType;
    }

    public Instant getTxnTimestamp() {
        return txnTimestamp;
    }

    public void setTxnTimestamp(Instant txnTimestamp) {
        this.txnTimestamp = txnTimestamp;
    }

    public String getCounterpartyId() {
        return counterpartyId;
    }

    public void setCounterpartyId(String counterpartyId) {
        this.counterpartyId = counterpartyId;
    }

    public String getCounterpartyName() {
        return counterpartyName;
    }

    public void setCounterpartyName(String counterpartyName) {
        this.counterpartyName = counterpartyName;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Map<String, Object> getGeoData() {
        return geoData;
    }

    public void setGeoData(Map<String, Object> geoData) {
        this.geoData = geoData;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }

    public Map<String, Object> getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(Map<String, Object> riskFactors) {
        this.riskFactors = riskFactors;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** Transaction types. */
    public enum TransactionType {
        CREDIT,
        DEBIT,
        TRANSFER,
        PAYMENT
    }

    /** Transaction channels. */
    public enum Channel {
        WEB,
        MOBILE,
        ATM,
        BRANCH,
        API,
        SWIFT,
        ACH
    }
}
