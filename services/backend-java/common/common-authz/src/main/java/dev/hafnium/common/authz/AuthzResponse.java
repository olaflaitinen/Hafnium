package dev.hafnium.common.authz;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * OPA authorization response.
 */
@Value
@Builder
@Jacksonized
public class AuthzResponse {

    Boolean result;

    @JsonProperty("decision_id")
    String decisionId;

    List<String> reasons;

    public boolean isAllowed() {
        return Boolean.TRUE.equals(result);
    }
}
