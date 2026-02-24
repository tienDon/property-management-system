package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.entity.OwnerSubscription;
import com.pms.propertymanagement.entity.Post;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.PostStatus;
import com.pms.propertymanagement.repository.PostRepository;
import com.pms.propertymanagement.service.OwnerSubscriptionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/moderator")
@RequiredArgsConstructor
public class ModeratorController {

    private final PostRepository postRepository;
    private final OwnerSubscriptionService ownerSubscriptionService;

    // ========== COMMON: Inject badge counts into every moderator page ==========

    @ModelAttribute
    public void addBadgeCounts(Model model) {
        model.addAttribute("pendingCount",
                postRepository.findByStatusOrderByCreatedAtAsc(PostStatus.PENDING_APPROVAL).size());
        model.addAttribute("reReviewCount",
                postRepository.findByStatusOrderByCreatedAtAsc(PostStatus.PENDING_REVISION).size());
    }

    // ========== DASHBOARD ==========

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";

        long totalPending = postRepository.findByStatusOrderByCreatedAtAsc(PostStatus.PENDING_APPROVAL).size();
        long totalReReview = postRepository.findByStatusOrderByCreatedAtAsc(PostStatus.PENDING_REVISION).size();

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

        List<Post> posts = postRepository.findByStatusOrderByCreatedAtAsc(PostStatus.PENDING_APPROVAL);
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

        List<Post> posts = postRepository.findByStatusOrderByCreatedAtAsc(PostStatus.PENDING_REVISION);
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

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new com.pms.propertymanagement.exception.ResourceNotFoundException("Bài đăng không tồn tại"));

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

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new com.pms.propertymanagement.exception.ResourceNotFoundException("Bài đăng không tồn tại"));

        if (post.getStatus() != PostStatus.PENDING_APPROVAL && post.getStatus() != PostStatus.PENDING_REVISION) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ duyệt được bài đang ở trạng thái chờ duyệt.");
            return "redirect:/moderator/posts/" + id;
        }

        User owner = post.getProperty().getOwner();

        String successMsg;
        if (post.getStatus() == PostStatus.PENDING_REVISION && post.getPausedAt() != null) {
            // PENDING_REVISION approval: compensate the pause duration.
            // Add (approvalTime - pausedAt) back to postExpiredAt so the owner doesn't lose time during review.
            java.time.LocalDateTime pausedAt = post.getPausedAt();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            long pausedSeconds = java.time.temporal.ChronoUnit.SECONDS.between(pausedAt, now);

            // If postExpiredAt is null or already past, base it from now; otherwise extend from frozen point.
            java.time.LocalDateTime baseExpiry = (post.getPostExpiredAt() != null)
                    ? post.getPostExpiredAt()
                    : now;
            java.time.LocalDateTime newExpiry = baseExpiry.plusSeconds(pausedSeconds);

            post.setPostExpiredAt(newExpiry);
            post.setPausedAt(null);
            post.setStatus(PostStatus.ACTIVE);
            post.setRejectionReason(null);
            postRepository.save(post);

            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(now, newExpiry);
            long compensatedDays = pausedSeconds / 86400;
            successMsg = "Đã duyệt lại bài đăng. Đã bù " + compensatedDays + " ngày chờ duyệt — còn " + daysLeft + " ngày hiển thị.";
        } else {
            // PENDING_APPROVAL (first approval): grant fresh days from owner's plan.
            OwnerSubscription subscription = ownerSubscriptionService.getOrCreateSubscription(owner);
            int durationDays = subscription.getManagementPlan().getPostDurationDays();
            post.approve(durationDays);
            postRepository.save(post);
            successMsg = "Đã duyệt bài đăng thành công. Bài sẽ hiển thị trong " + durationDays + " ngày theo gói của chủ nhà.";
        }

        redirectAttributes.addFlashAttribute("successMessage", successMsg);
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

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new com.pms.propertymanagement.exception.ResourceNotFoundException("Bài đăng không tồn tại"));

        if (post.getStatus() != PostStatus.PENDING_APPROVAL && post.getStatus() != PostStatus.PENDING_REVISION) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ từ chối được bài đang ở trạng thái chờ duyệt.");
            return "redirect:/moderator/posts/" + id;
        }

        post.reject(reason.trim());
        postRepository.save(post);

        redirectAttributes.addFlashAttribute("successMessage", "Đã yêu cầu chỉnh sửa bài đăng.");
        return "redirect:/moderator/posts/" + id;
    }
}
