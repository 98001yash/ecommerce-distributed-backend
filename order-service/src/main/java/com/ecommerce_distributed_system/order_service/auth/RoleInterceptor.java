package com.ecommerce_distributed_system.order_service.auth;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod method = (HandlerMethod) handler;

        RoleAllowed roleAllowed = method.getMethodAnnotation(RoleAllowed.class);

        if (roleAllowed == null) {
            roleAllowed = method.getBeanType().getAnnotation(RoleAllowed.class);
        }

        if (roleAllowed == null) {
            return true;
        }

        List<String> roles = UserContextHolder.getCurrentUserRoles();

        if (roles == null || roles.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            log.warn("Access denied: No roles found for user");
            return false;
        }

        Set<String> userRoles = new HashSet<>(roles);
        Set<String> allowedRoles = new HashSet<>(Arrays.asList(roleAllowed.value()));

        boolean hasAccess = userRoles.stream().anyMatch(allowedRoles::contains);

        if (!hasAccess) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            log.warn("Access denied: user roles {} required {}", userRoles, allowedRoles);
        }

        return hasAccess;
    }
}