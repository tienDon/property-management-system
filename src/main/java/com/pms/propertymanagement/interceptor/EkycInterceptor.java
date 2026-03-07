package com.pms.propertymanagement.interceptor;

import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class EkycInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        if (uri.startsWith("/ekyc") || uri.startsWith("/login") || uri.startsWith("/register") || uri.startsWith("/logout")) {
            return true;
        }

        if (!(uri.startsWith("/owner") || uri.startsWith("/tenant"))) {
            return true;
        }

        User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null) {
            response.sendRedirect(uri.startsWith("/owner") ? "/login/owner" : "/login");
            return false;
        }

        User user = userRepository.findById(sessionUser.getId()).orElse(sessionUser);
        request.getSession().setAttribute("user", user);

        if (requiresEkyc(user) && !user.isEkycVerified()) {
            response.sendRedirect("/ekyc?next=" + uri);
            return false;
        }

        return true;
    }

    private boolean requiresEkyc(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals("USER") || r.getName().equals("OWNER"));
    }
}

