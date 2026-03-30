package com.skillforge.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.skillforge.domain.user.dto.AuthResponse;
import com.skillforge.domain.user.service.AuthService;
import com.skillforge.exception.UnauthorizedException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final AuthService authService;

    @Value("${app.frontend.oauth-success-url:http://localhost:3000/oauth-success}")
    private String frontendOauthSuccessUrl;

    public OAuth2AuthenticationSuccessHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String email = stringAttr(principal, "email");
        String googleId = stringAttr(principal, "sub");
        String name = stringAttr(principal, "name");
        String picture = stringAttr(principal, "picture");

        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Google account email is missing");
        }

        AuthResponse auth = authService.googleAuthWithOAuthProfile(email, googleId, name, picture);
        String encodedToken = URLEncoder.encode(auth.getAccessToken(), StandardCharsets.UTF_8);
        String redirectUrl = frontendOauthSuccessUrl + "?token=" + encodedToken;

        log.info("OAuth request successful for {}, redirecting to {}", email, frontendOauthSuccessUrl);
        response.sendRedirect(redirectUrl);
    }

    private String stringAttr(OAuth2User principal, String key) {
        Object value = principal.getAttributes().get(key);
        return value instanceof String str ? str : null;
    }
}
