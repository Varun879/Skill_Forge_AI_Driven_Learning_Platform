package com.skillforge.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(OAuth2RequestLoggingFilter.class);

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:}")
    private String googleClientSecret;

    @Value("${app.oauth.enforce-config:false}")
    private boolean enforceOauthConfig;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if ("/oauth2/authorization/google".equals(path)) {
            boolean invalidClientId = googleClientId == null || googleClientId.isBlank()
                || "GOOGLE_CLIENT_ID_NOT_SET".equals(googleClientId);
            boolean invalidSecret = googleClientSecret == null || googleClientSecret.isBlank()
                || "GOOGLE_CLIENT_SECRET_NOT_SET".equals(googleClientSecret);

            if (invalidClientId || invalidSecret) {
                if (enforceOauthConfig) {
                    log.warn("OAuth request rejected: GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET not configured (strict mode enabled)");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"message\":\"Google OAuth is not configured. Set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET.\"}");
                    response.flushBuffer();
                    return;
                }

                log.warn("Google OAuth client credentials are missing/placeholder. Continuing because strict mode is disabled.");
            }

            String expectedRedirectUri = request.getScheme() + "://" + request.getServerName()
                    + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort())
                    + "/login/oauth2/code/google";
            log.info("OAuth request received for Google authorization endpoint, expected redirect URI={}", expectedRedirectUri);
        }
        if ("/login/oauth2/code/google".equals(path)) {
            log.info("OAuth callback received on redirect URI {}{}",
                    path,
                    request.getQueryString() == null ? "" : "?" + request.getQueryString());
        }

        filterChain.doFilter(request, response);
    }
}
