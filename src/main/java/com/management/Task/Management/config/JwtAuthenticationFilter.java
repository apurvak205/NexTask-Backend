package com.management.task.management.config;

import com.management.task.management.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private static final String ALLOWED_ORIGIN =
            "https://nex-task-frontend-alpha.vercel.app";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // ✅ Auth aur OAuth URLs skip karo — inhe JWT filter ki zaroorat nahi
        if (path.startsWith("/api/auth")
                || path.startsWith("/oauth2")
                || path.startsWith("/login/oauth2")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        // ✅ Token bilkul nahi hai
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (path.startsWith("/api/")) {
                // ✅ API call pe seedha 401 do — Google redirect nahi
                sendUnauthorized(response, "Token missing");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            String username = jwtService.extractUsername(jwt);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // ✅ Token valid — authentication set karo
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                } else {
                    // ✅ Token invalid — API pe 401 do
                    if (path.startsWith("/api/")) {
                        sendUnauthorized(response, "Token invalid");
                        return;
                    }
                }
            }

        } catch (Exception e) {
            // ✅ Token expired ya corrupt — Google redirect nahi, 401 do
            logger.error("JWT processing failed: " + e.getMessage());
            if (path.startsWith("/api/")) {
                sendUnauthorized(response, "Token expired or invalid");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // ✅ Helper method — baar baar likhne ki zaroorat nahi
    private void sendUnauthorized(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}