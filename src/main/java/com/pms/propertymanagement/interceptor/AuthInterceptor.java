package com.pms.propertymanagement.interceptor;

import com.pms.propertymanagement.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();

        User user = (User) request.getSession().getAttribute("user");

        if (uri.contains("/owner")) {
            if (user == null) {
                response.sendRedirect("/");
                return false;
            }
            boolean isOwner = user.getRoles().stream().allMatch(role -> role.getName().equals("OWNER"));
            if (!isOwner) {
                response.sendRedirect("/403");
                return false;
            }
        }

        if (uri.contains("/staff")) {
            if (user == null || user.getRoles().stream().noneMatch(r -> r.getName().equals("STAFF"))) {
                response.sendRedirect("/403");
                return false;
            }
        }

        if (uri.contains("/admin")) {
            if (user == null || user.getRoles().stream().noneMatch(r -> r.getName().equals("ADMIN"))) {
                response.sendRedirect("/403");
                return false;
            }
        }

        if (uri.contains("/moderator")) {
            if (user == null || user.getRoles().stream().noneMatch(r -> r.getName().equals("MODERATOR"))) {
                response.sendRedirect("/login/owner");
                return false;
            }
        }

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
