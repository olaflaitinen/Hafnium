package dev.hafnium.identity.domain;

import dev.hafnium.common.model.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Customer domain entity.
 *
 * <p>
 * Represents a customer in the KYC workflow. Customers may be individuals or
 * businesses
 * undergoing identity verification.
 */
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

    @Id
    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false)
    private CustomerType customerType = CustomerType.INDIVIDUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomerStatus status = CustomerStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_tier")
    private RiskTier riskTier;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Version
    @Column(name = "version")
    private Long version;

    public Customer() {
        this.customerId = UUID.randomUUID();
    }

    public Customer(UUID tenantId, String externalId) {
        this();
        this.tenantId = tenantId;
        this.externalId = externalId;
    }

    // Getters and setters

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    @Override
    public UUID getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
    }

    public RiskTier getRiskTier() {
        return riskTier;
    }

    public void setRiskTier(RiskTier riskTier) {
        this.riskTier = riskTier;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public Long getVersion() {
        return version;
    }

    @Override
    public void setVersion(Long version) {
        this.version = version;
    }

    /** Customer types. */
    public enum CustomerType {
        INDIVIDUAL,
        BUSINESS
    }

    /** Customer verification statuses. */
    public enum CustomerStatus {
        PENDING,
        DOCUMENTS_REQUIRED,
        IN_REVIEW,
        VERIFIED,
        REJECTED
    }

    /** Customer risk tiers. */
    public enum RiskTier {
        LOW,
        MEDIUM,
        HIGH
    }
}
