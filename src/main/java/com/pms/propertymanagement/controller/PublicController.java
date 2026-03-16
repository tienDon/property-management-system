package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.request.ContactRequest;
import com.pms.propertymanagement.dto.request.PostFilterDTO;
import com.pms.propertymanagement.dto.response.PostPublicResponse;
import com.pms.propertymanagement.entity.Post;
import com.pms.propertymanagement.service.CategoryService;
import com.pms.propertymanagement.service.PostService;
import com.pms.propertymanagement.service.PropertyCommentService;
import com.pms.propertymanagement.service.PropertyService;
import com.pms.propertymanagement.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Controller
@RequiredArgsConstructor
public class PublicController {

    private final CategoryService categoryService;
    private final PropertyService propertyService;
    private final PostService postService;
    private final ReviewService reviewService;
    private final PropertyCommentService propertyCommentService;
    private final com.pms.propertymanagement.repository.LandlordRatingRepository landlordRatingRepository;
    private final com.pms.propertymanagement.repository.ContractRepository contractRepository;

    @GetMapping("/")
    public String index(Model model) {
        // NEW ARCHITECTURE: Get marketplace posts (ACTIVE only), not all properties
        List<PostPublicResponse> properties = postService.getAllMarketplacePosts()
                .stream().map(PostPublicResponse::from).toList();

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("content", "public/home");
        model.addAttribute("properties", properties);

        return "layout/public-main";
    }

    @GetMapping("/public/category/{id}")
    public String listByCategory(@PathVariable("id") Long categoryId, Model model) {
        // Giữ luồng cũ: hiển thị theo danh mục, không phân trang nâng cao
        List<PostPublicResponse> properties = postService.getMarketplacePostsByCategory(categoryId)
                .stream().map(PostPublicResponse::from).toList();

        model.addAttribute("properties", properties);
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("allAmenities", propertyService.getAllAmenities());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("content", "public/property-list");
        return "layout/public-main";
    }

    /**
     * Tìm kiếm danh sách bài đăng theo nhiều tiêu chí (khu vực, loại, giá, tiện ích, diện tích, số phòng, keyword)
     * URL: /public/search
     */
    @GetMapping("/public/search")
    public String search(
            PostFilterDTO filter,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,DESC") String sort,
            @RequestParam(value = "priceQuick", required = false) String priceQuick,
            @RequestParam(value = "areaRange", required = false) String areaRange,
            Model model
    ) {
        // Map quick price range (priceQuick) -> priceMin/priceMax
        if (priceQuick != null && !priceQuick.isBlank()) {
            String[] p = priceQuick.split("-");
            try {
                Integer min = Integer.parseInt(p[0]);
                Integer max = p.length > 1 ? Integer.parseInt(p[1]) : null;
                filter.setPriceMin(min != 0 ? min : null);
                filter.setPriceMax(max != null && max != 0 ? max : null);
            } catch (NumberFormatException ignored) {
            }
        }

        // Map area range (m2) -> areaMin/areaMax
        if (areaRange != null && !areaRange.isBlank()) {
            String[] a = areaRange.split("-");
            try {
                Double min = Double.parseDouble(a[0]);
                Double max = a.length > 1 ? Double.parseDouble(a[1]) : null;
                filter.setAreaMin(min != 0 ? min : null);
                filter.setAreaMax(max != null && max != 0 ? max : null);
            } catch (NumberFormatException ignored) {
            }
        }

        // Parse sort param: field,DESC|ASC
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && "ASC".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(direction, sortField));

        Page<com.pms.propertymanagement.entity.Post> resultPage = postService.searchPosts(filter, pageable);
        List<PostPublicResponse> properties = resultPage.getContent()
                .stream().map(PostPublicResponse::from).toList();

        model.addAttribute("properties", properties);
        model.addAttribute("page", resultPage);
        model.addAttribute("allAmenities", propertyService.getAllAmenities());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("filter", filter);
        model.addAttribute("sort", sort);

        model.addAttribute("content", "public/property-list");
        return "layout/public-main";
    }

    @GetMapping("/public/property/{slug}")
    public String propertyDetail(@PathVariable("slug") String slug, Model model, jakarta.servlet.http.HttpSession session) {
        Post post = postService.getMarketplacePostBySlug(slug)
                .orElseThrow(() -> new com.pms.propertymanagement.exception.ResourceNotFoundException("Post not found or not available"));

        postService.incrementView(slug);

        PostPublicResponse p = PostPublicResponse.from(post);
        model.addAttribute("p", p);
        
        // Add reviews and landlord rating
        model.addAttribute("reviews", reviewService.getReviewsByProperty(p.getId()));
        model.addAttribute("starDistribution", reviewService.getStarDistribution(p.getId()));
        model.addAttribute("comments", propertyCommentService.getCommentsByProperty(p.getId()));
        landlordRatingRepository.findByLandlordId(p.getOwnerId())
                .ifPresent(rating -> model.addAttribute("landlordRating", rating));

        // Eligibility to review
        boolean isEligible = false;
        com.pms.propertymanagement.entity.User user = (com.pms.propertymanagement.entity.User) session.getAttribute("user");
        if (user != null) {
            List<com.pms.propertymanagement.entity.Contract> endedContracts = contractRepository.findEndedContractsByUser(p.getId(), user.getEmail(), user.getPhone());
            for (com.pms.propertymanagement.entity.Contract c : endedContracts) {
                if (reviewService.getReviewByContractAndTenant(c.getId(), user.getId()) == null) {
                    isEligible = true;
                    model.addAttribute("eligibleContractId", c.getId());
                    break;
                }
            }
        }
        model.addAttribute("isEligibleToReview", isEligible);
        model.addAttribute("currentUser", user);

        model.addAttribute("content", "public/property-detail");
        model.addAttribute("contact", new ContactRequest());

        return "layout/public-main";
    }

}
