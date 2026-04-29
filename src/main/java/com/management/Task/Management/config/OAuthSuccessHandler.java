package com.management.task.management.config;

import com.management.task.management.exception.BadRequestException;
import com.management.task.management.model.AuthProvider;
import com.management.task.management.model.User;
import com.management.task.management.repository.UserRepository;
import com.management.task.management.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final String frontendBaseUrl;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public OAuthSuccessHandler(
            UserRepository userRepository,
            JwtService jwtService,
            @Value("${APP_FRONTEND_URL:https://nex-task-frontend-alpha.vercel.app}") String frontendBaseUrl
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.frontendBaseUrl = frontendBaseUrl;
    }


    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        if (email == null || name == null) {
            throw new BadRequestException("Google account did not provide required profile details");
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    newUser.setProvider(AuthProvider.GOOGLE);
                    return userRepository.save(newUser);
                });

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles("USER")
                        .build();

        String token = jwtService.generateToken(userDetails, user.getName());
        response.sendRedirect(frontendBaseUrl + "/dashboard.html?token=" + token);
    }
}
