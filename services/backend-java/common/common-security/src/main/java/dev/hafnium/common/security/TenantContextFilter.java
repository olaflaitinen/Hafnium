package dev.hafnium.common.security;

import jakarta.servlet.*;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Filter that clears the TenantContext after each request.
 *
 * <p>
 * This ensures ThreadLocal state is cleaned up properly, especially important
 * in servlet
 * containers with thread pooling.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class TenantContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
