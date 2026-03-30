package com.skillforge.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);

    @Value("${app.frontend.oauth-error-url:http://localhost:3000/oauth-error}")
    private String frontendOauthErrorUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String message = mapErrorMessage(exception);
        String redirectUrl = frontendOauthErrorUrl + "?error="
                + URLEncoder.encode(message, StandardCharsets.UTF_8);

        log.warn("OAuth authentication failed on {}: {}", request.getRequestURI(), exception.getMessage());
        response.sendRedirect(redirectUrl);
    }

    private String mapErrorMessage(AuthenticationException exception) {
        String msg = exception.getMessage() == null ? "OAuth login failed" : exception.getMessage().toLowerCase();
        if (msg.contains("redirect_uri") || msg.contains("invalid_request")) {
            return "Invalid redirect URI";
        }
        if (msg.contains("access_denied")) {
            return "Google login was cancelled";
        }
        return "Google authentication failed";
    }
}
