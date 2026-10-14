package com.questboard.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIX.length());
        if (!jwtService.isValid(token) || !jwtService.isAccessToken(token)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            UUID userId = jwtService.extractUserId(token);
            String username = jwtService.extractUsername(token);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                // dupla checagem de ID
                if (userDetails instanceof CustomUserDetails cud && !cud.getId().equals(userId)) {
                    chain.doFilter(request, response);
                    return;
                }
                var auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            log.debug("Falha ao autenticar via JWT: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }
}