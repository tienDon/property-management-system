package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.entity.Post;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.PostStatus;
import com.pms.propertymanagement.service.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/moderator")
@RequiredArgsConstructor
@Slf4j
public class ModeratorController {

    private final PostService postService;

    // ========== COMMON: Inject badge counts into every moderator page ==========

    @ModelAttribute
    public void addBadgeCounts(Model model) {
        model.addAttribute("pendingCount",
                postService.getPostsByStatus(PostStatus.PENDING_APPROVAL).size());
        model.addAttribute("reReviewCount",
                postService.getPostsByStatus(PostStatus.PENDING_REVISION).size());
    }

    // ========== DASHBOARD ==========

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";

        long totalPending = postService.getPostsByStatus(PostStatus.PENDING_APPROVAL).size();
        long totalReReview = postService.getPostsByStatus(PostStatus.PENDING_REVISION).size();

        model.addAttribute("totalPending", totalPending);
        model.addAttribute("totalReReview", totalReReview);
        model.addAttribute("activeMenu", "");
        model.addAttribute("activeSection", "manage");
        model.addAttribute("content", "moderator/management/dashboard");
        return "layout/moderator-layout";
    }

    // ========== POST LIST ==========

    @GetMapping("/posts/pending")
    public String pendingPosts(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";

        List<Post> posts = postService.getPostsByStatus(PostStatus.PENDING_APPROVAL);
        model.addAttribute("posts", posts);
        model.addAttribute("activeMenu", "posts");
        model.addAttribute("activeTab", "pending");
        model.addAttribute("activeSection", "manage");
        model.addAttribute("content", "moderator/management/posts/list");
        return "layout/moderator-layout";
    }

    @GetMapping("/posts/re-review")
    public String reReviewPosts(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";

        List<Post> posts = postService.getPostsByStatus(PostStatus.PENDING_REVISION);
        model.addAttribute("posts", posts);
        model.addAttribute("activeMenu", "posts");
        model.addAttribute("activeTab", "re-review");
        model.addAttribute("activeSection", "manage");
        model.addAttribute("content", "moderator/management/posts/list");
        return "layout/moderator-layout";
    }

    // ========== CHAT (placeholder) ==========

    @GetMapping("/chat")
    public String chat(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";
        model.addAttribute("activeMenu", "chat");
        model.addAttribute("activeSection", "chat");
        model.addAttribute("content", "moderator/chat/index");
        return "layout/moderator-layout";
    }

    // ========== POST DETAIL ==========

    @GetMapping("/posts/{id}")
    public String postDetail(@PathVariable Long id,
                             HttpSession session,
                             Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";

        Post post = postService.findPostById(id);

        model.addAttribute("post", post);
        model.addAttribute("activeMenu", "posts");
        String tab = switch (post.getStatus()) {
            case PENDING_REVISION -> "re-review";
            default -> "pending";
        };
        model.addAttribute("activeTab", tab);
        model.addAttribute("activeSection", "manage");
        model.addAttribute("content", "moderator/management/posts/detail");
        return "layout/moderator-layout";
    }

    // ========== APPROVE ==========

    @PostMapping("/posts/{id}/approve")
    public String approvePost(@PathVariable Long id,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";

        try {
            postService.approvePostByModerator(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt bài đăng thành công.");
            log.info("Moderator {} approved post {}", user.getUsername(), id);
        } catch (Exception e) {
            log.error("Error approving post {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể duyệt bài: " + e.getMessage());
        }

        return "redirect:/moderator/posts/" + id;
    }

    // ========== REJECT ==========

    @PostMapping("/posts/{id}/reject")
    public String rejectPost(@PathVariable Long id,
                             @RequestParam(name = "reason", required = false) String reason,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";

        if (!StringUtils.hasText(reason)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập lý do từ chối.");
            return "redirect:/moderator/posts/" + id;
        }

        try {
            postService.rejectPostByModerator(id, reason.trim());
            redirectAttributes.addFlashAttribute("successMessage", "Đã yêu cầu chỉnh sửa bài đăng.");
            log.info("Moderator {} rejected post {}", user.getUsername(), id);
        } catch (Exception e) {
            log.error("Error rejecting post {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể từ chối bài: " + e.getMessage());
        }

        return "redirect:/moderator/posts/" + id;
    }
}
