package dev.hafnium.cases.dto;

/**
 * Update case request DTO.
 */
public record UpdateCaseRequest(
        String status,
        String priority,
        String assignedTo,
        String notes) {
}
