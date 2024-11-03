package org.gh.afriluck.afriluckussd.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.gh.afriluck.afriluckussd.dto.ErrorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class IpWhitelistFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(IpWhitelistFilter.class);

    @Value("${whitelisted.ips}")
    private String whitelistedIps;

    private List<String> allowedIpList;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        allowedIpList = Arrays.asList(whitelistedIps.split(","));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = httpRequest.getHeader("X-Forwarded-For");

        if (clientIp != null) {
            String[] ipArray = clientIp.split(",");
            clientIp = ipArray[0].trim();
        } else {
            clientIp = httpRequest.getRemoteAddr();
        }

        logger.info("Request from IP: {}", clientIp);

        if (!allowedIpList.contains(clientIp)) {
            logger.warn("Access denied for IP: {}", clientIp);
            sendErrorResponse(httpResponse, "Access denied for IP: " + clientIp, HttpStatus.FORBIDDEN.value());
            return; // Stop further processing
        }

        chain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Create a formatted error response
        ErrorResponse errorResponse = new ErrorResponse(message, status);

        // Convert to JSON (you can use a library like Jackson or Gson here)
        String jsonResponse = String.format("{\"message\":\"%s\", \"status\":%d, \"timestamp\":%d}",
                errorResponse.getMessage(), errorResponse.getStatus(), errorResponse.getTimestamp());

        out.print(jsonResponse);
        out.flush();
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}


