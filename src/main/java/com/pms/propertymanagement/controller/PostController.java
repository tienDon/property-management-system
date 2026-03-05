package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.PostPackageDTO;
import com.pms.propertymanagement.dto.response.PostAnalyticsDTO;
import com.pms.propertymanagement.dto.response.PostOwnerResponse;
import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.entity.UserWallet;
import com.pms.propertymanagement.service.PostingPackageService;
import com.pms.propertymanagement.service.PostService;
import com.pms.propertymanagement.service.PropertyService;
import com.pms.propertymanagement.service.WalletService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for owner's post management
 * NEW ARCHITECTURE: Manages Post lifecycle separately from Property
 */
@Controller
@RequestMapping("/owner/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final PostingPackageService postingPackageService;
    private final PropertyService propertyService;
    private final WalletService walletService;

    /**
     * Owner post dashboard - list all posts grouped by status
     * URL: /owner/posts
     */
    @GetMapping
    public String listPosts(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<PostOwnerResponse> allPosts = postService.getPostsByOwner(user.getId());

        // Group posts by status for better UI organization
        Map<String, List<PostOwnerResponse>> postsByStatus = allPosts.stream()
                .collect(Collectors.groupingBy(post -> post.getStatus().name()));

        // Check if there are available properties for post creation
        List<Property> availableProperties = propertyService.getActivePropertiesWithoutPost(user.getId());

        model.addAttribute("allPosts", allPosts);
        model.addAttribute("postsByStatus", postsByStatus);
        model.addAttribute("hasAvailableProperties", !availableProperties.isEmpty());
        model.addAttribute("activeMenu", "posts");
        
        // Add content template path
        model.addAttribute("content", "owner/post/list");
        
        return "layout/owner-layout";
    }

    /**
     * Show create post form - lets owner select a property and fill post details
     * URL: GET /owner/posts/create
     */
    @GetMapping("/create")
    public String showCreatePostForm(HttpSession session, Model model, RedirectAttributes ra) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Property> availableProperties = propertyService.getActivePropertiesWithoutPost(user.getId());

        if (availableProperties.isEmpty()) {
            ra.addFlashAttribute("errorMessage", 
                "Bạn chưa có nhà trọ nào để tạo bài đăng. Vui lòng thêm nhà trọ trước.");
            return "redirect:/owner/posts";
        }

        model.addAttribute("availableProperties", availableProperties);
        model.addAttribute("activeMenu", "posts");
        model.addAttribute("content", "owner/post/create");
        return "layout/owner-layout";
    }

    /**
     * Handle post creation form submission
     * URL: POST /owner/posts/create
     */
    @PostMapping("/create")
    public String createPost(
            @RequestParam Long propertyId,
            @RequestParam String title,
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) String description,
            HttpSession session,
            RedirectAttributes ra) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // Auto-generate slug from title if not provided
            if (slug == null || slug.isBlank()) {
                slug = generateSlug(title + "-" + propertyId);
            } else {
                slug = generateSlug(slug);
            }

            postService.createPostForProperty(propertyId, user.getId(), title, slug, description);
            ra.addFlashAttribute("successMessage", 
                "Tạo bài đăng thành công! Bài đăng đang chờ staff duyệt. Sau khi được duyệt sẽ hiển thị 7 ngày dùng thử miễn phí.");
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/owner/posts/create";
        } catch (Exception ex) {
            log.error("Error creating post for property {}: {}", propertyId, ex.getMessage());
            ra.addFlashAttribute("errorMessage", "Lỗi tạo bài đăng: " + ex.getMessage());
            return "redirect:/owner/posts/create";
        }

        return "redirect:/owner/posts";
    }

    private String generateSlug(String input) {
        if (input == null) return "";
        return input.toLowerCase()
            .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
            .replaceAll("[èéẹẻẽêềếệểễ]", "e")
            .replaceAll("[ìíịỉĩ]", "i")
            .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
            .replaceAll("[ùúụủũưừứựửữ]", "u")
            .replaceAll("[ỳýỵỷỹ]", "y")
            .replaceAll("đ", "d")
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-+|-+$", "");
    }

    /**
     * Resubmit a rejected post for approval again
     * URL: POST /owner/posts/{id}/resubmit
     */
    @PostMapping("/{id}/resubmit")
    public String resubmitPost(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes ra) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            postService.resubmitPost(id, user.getId());
            ra.addFlashAttribute("successMessage",
                    "Đã gửi lại bài đăng để duyệt. Vui lòng chờ staff xem xét.");
        } catch (Exception e) {
            log.error("Error resubmitting post {}: {}", id, e.getMessage());
            ra.addFlashAttribute("errorMessage", "Không thể gửi lại: " + e.getMessage());
        }

        return "redirect:/owner/posts";
    }

    /**
     * View post analytics detail
     * URL: /owner/posts/{id}/analytics
     */
    @GetMapping("/{id}/analytics")
    public String viewAnalytics(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        PostAnalyticsDTO analytics = postService.getPostAnalytics(id);
        model.addAttribute("analytics", analytics);
        model.addAttribute("activeMenu", "posts");
        model.addAttribute("content", "owner/post/analytics");
        
        return "layout/owner-layout";
    }

    /**
     * Hide post (owner action)
     * URL: POST /owner/posts/{id}/hide
     */
    @PostMapping("/{id}/hide")
    public String hidePost(@PathVariable Long id, 
                          HttpSession session, 
                          RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            postService.hidePost(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã ẩn bài đăng thành công");
        } catch (Exception e) {
            log.error("Error hiding post {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể ẩn bài đăng: " + e.getMessage());
        }

        return "redirect:/owner/posts";
    }

    /**
     * Show post (owner action)
     * URL: POST /owner/posts/{id}/show
     */
    @PostMapping("/{id}/show")
    public String showPost(@PathVariable Long id, 
                          HttpSession session, 
                          RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            postService.showPost(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hiển thị bài đăng thành công");
        } catch (Exception e) {
            log.error("Error showing post {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hiển thị bài đăng: " + e.getMessage());
        }

        return "redirect:/owner/posts";
    }

    /**
     * Show renew post form
     * URL: GET /owner/posts/{id}/renew
     */
    @GetMapping("/{id}/renew")
    public String showRenewForm(@PathVariable Long id, 
                                HttpSession session, 
                                Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        PostAnalyticsDTO analytics = postService.getPostAnalytics(id);
        
        // Load available posting packages for renewal
        List<PostPackageDTO> packages = postingPackageService.getAllPackages();
        
        model.addAttribute("analytics", analytics);
        model.addAttribute("postPackages", packages);
        model.addAttribute("activeMenu", "posts");
        model.addAttribute("content", "owner/post/renew");
        
        return "layout/owner-layout";
    }

    /**
     * Renew post with posting package (deducts wallet)
     * URL: POST /owner/posts/{id}/renew
     */
    @PostMapping("/{id}/renew")
    public String renewPost(@PathVariable Long id,
                           @RequestParam Long postPackageId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            var postPackage = postingPackageService.getById(postPackageId);
            postService.purchaseDuration(id, postPackage);
            
            // CRITICAL: Refresh wallet in session after transaction
            UserWallet updatedWallet = walletService.getOrCreateWallet(user);
            session.setAttribute("userWallet", updatedWallet);
            session.setAttribute("lastWalletUpdate", System.currentTimeMillis());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                    String.format("Đã gia hạn bài đăng thêm %d ngày. Đã trừ %,d VND từ ví.", 
                        postPackage.getUsageLimit(), postPackage.getPrice()));
        } catch (IllegalStateException e) {
            // Wallet balance insufficient
            log.error("Insufficient balance for user renewing post {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error renewing post {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Không thể gia hạn bài đăng: " + e.getMessage());
        }

        return "redirect:/owner/posts";
    }

    /**
     * Show edit post content form
     * URL: GET /owner/posts/{id}/edit
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, 
                               HttpSession session, 
                               Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        PostOwnerResponse post = postService.getPostsByOwner(user.getId())
                .stream()
                .filter(p -> p.getPostId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Post not found"));

        model.addAttribute("post", post);
        model.addAttribute("activeMenu", "posts");
        model.addAttribute("content", "owner/post/edit");
        
        return "layout/owner-layout";
    }

    /**
     * Update post marketing content
     * URL: POST /owner/posts/{id}/update
     */
    @PostMapping("/{id}/update")
    public String updatePost(@PathVariable Long id,
                            @RequestParam String title,
                            @RequestParam String slug,
                            @RequestParam String description,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            postService.updatePostContent(id, title, slug, description);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã cập nhật bài đăng. Bài đăng đang tạm dừng và chờ moderator duyệt lại trước khi tiếp tục hiển thị.");
        } catch (Exception e) {
            log.error("Error updating post {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Không thể cập nhật bài đăng: " + e.getMessage());
        }

        return "redirect:/owner/posts";
    }
}
