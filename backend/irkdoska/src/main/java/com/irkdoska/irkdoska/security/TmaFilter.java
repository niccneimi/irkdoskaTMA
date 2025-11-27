package com.irkdoska.irkdoska.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.irkdoska.irkdoska.exception.ExpiredTmaException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TmaFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TmaFilter.class);

    @Value("${TG_BOT_TOKEN}")
    private String tgBotToken;

    private TmaCore tmaCore;

    @Autowired
    public void setTmaCore(TmaCore tmaCore) {
        this.tmaCore = tmaCore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String initRawData = null;
        TmaUserPrincipal principal = null;
        UsernamePasswordAuthenticationToken auth = null;
        try {
            String headerAuth = request.getHeader("Authorization");
            if (headerAuth != null && headerAuth.startsWith("tma ")) {
                initRawData = headerAuth.substring(4);
                logger.info("AUTH TRY: {}", initRawData);
            }
            if (initRawData != null) {
                    if (tmaCore.validate(tgBotToken, initRawData)) {
                        principal = tmaCore.getPrincipalFromTma(initRawData);
                        logger.info("Successfully validated {}: {}", principal.getRole(), principal.getTelegramId());
                    }
                    if (principal != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        auth = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
            }
        } catch (ExpiredTmaException e) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
            return;
        } catch (Exception e) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "Invalid token");
            return;
        }
        filterChain.doFilter(request, response);
    }

}
