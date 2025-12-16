package dev.hafnium.customer.service;

import dev.hafnium.customer.domain.Customer;
import dev.hafnium.customer.dto.CustomerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Customer entity to DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface CustomerMapper {

    /**
     * Map Customer entity to CustomerResponse DTO.
     *
     * @param customer Customer entity
     * @return Customer response DTO
     */
    @Mapping(target = "customerType", expression = "java(customer.getCustomerType().name())")
    @Mapping(target = "status", expression = "java(customer.getStatus().name())")
    @Mapping(target = "riskTier", expression = "java(customer.getRiskTier() != null ? customer.getRiskTier().name() : null)")
    CustomerResponse toResponse(Customer customer);
}
