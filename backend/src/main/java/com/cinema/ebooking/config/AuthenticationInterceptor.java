package com.cinema.ebooking.config;

import com.cinema.ebooking.service.AuthService;
import com.cinema.ebooking.entity.User;
import com.cinema.ebooking.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final UserRepository userRepository;

    public AuthenticationInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        boolean publicRequest =
            path.equals("/api/auth/register")
                || path.equals("/api/auth/csrf")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/verify")
                || path.equals("/api/auth/forgot-password")
                || path.equals("/api/auth/reset-password")
                || (path.startsWith("/api/movies")
                    && HttpMethod.GET.matches(method));

        if (publicRequest) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null
            || session.getAttribute(AuthService.USER_ID) == null) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Sign in to continue."
            );
            return false;
        }

        Long userId = (Long) session.getAttribute(AuthService.USER_ID);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getAccountStatus() != User.AccountStatus.ACTIVE) {
            session.invalidate();
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "This session is no longer active."
            );
            return false;
        }

        if (path.startsWith("/api/admin")
            || (path.startsWith("/api/movies")
                && !HttpMethod.GET.matches(method))) {
            if (user.getRole() != User.Role.ADMIN) {
                response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Administrator access is required."
                );
                return false;
            }
        }

        return true;
    }
}
