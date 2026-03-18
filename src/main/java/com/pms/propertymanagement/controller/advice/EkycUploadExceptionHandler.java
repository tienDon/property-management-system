package com.pms.propertymanagement.controller.advice;

import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.entity.Role;
import com.pms.propertymanagement.service.EkycService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

@ControllerAdvice
@RequiredArgsConstructor
public class EkycUploadExceptionHandler {
    private final EkycService ekycService;

    @ExceptionHandler({
            MaxUploadSizeExceededException.class,
            MultipartException.class,
            MissingServletRequestPartException.class
    })
    public String handleEkycUploadErrors(Exception ex,
                                        HttpServletRequest request,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith("/ekyc")) {
            throw ex instanceof RuntimeException ? (RuntimeException) ex : new RuntimeException(ex);
        }

        List<String> errors;
        if (ex instanceof MaxUploadSizeExceededException) {
            errors = List.of("Kích thước file vượt quá giới hạn cho phép");
        } else if (ex instanceof MissingServletRequestPartException) {
            errors = List.of("Vui lòng upload đủ 3 ảnh (mặt trước, mặt sau, khuôn mặt)");
        } else {
            errors = List.of("Upload thất bại, vui lòng thử lại");
        }

        redirectAttributes.addFlashAttribute("errors", errors);

        String next = request.getParameter("next");
        redirectAttributes.addAttribute("next", safeNextOrDefault(next, session));

        return "redirect:/ekyc";
    }

    private String safeNextOrDefault(String next, HttpSession session) {
        if (next != null && isValidRedirectUrl(next)) return next;

        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "/home";

        User user = ekycService.refreshUser(sessionUser.getId()).orElse(sessionUser);
        session.setAttribute("user", user);

        Set<Role> roles = user.getRoles();
        if (roles != null && roles.stream().anyMatch(r -> "OWNER".equals(r.getName()))) {
            return "/owner";
        }
        return "/tenant/rooms";
    }

    private boolean isValidRedirectUrl(String url) {
        return url.startsWith("/") && !url.startsWith("//");
    }
}
