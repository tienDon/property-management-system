package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.request.ContactRequest;
import com.pms.propertymanagement.dto.response.PostPublicResponse;
import com.pms.propertymanagement.entity.Post;
import com.pms.propertymanagement.service.CategoryService;
import com.pms.propertymanagement.service.PostService;
import com.pms.propertymanagement.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PublicController {

    private final CategoryService categoryService;
    private final PropertyService propertyService;
    private final PostService postService;

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
        List<PostPublicResponse> properties = postService.getMarketplacePostsByCategory(categoryId)
                .stream().map(PostPublicResponse::from).toList();

        model.addAttribute("properties", properties);
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("allAmenities", propertyService.getAllAmenities());
        model.addAttribute("categoryId", categoryId);

        model.addAttribute("content", "public/property-list");
        return "layout/public-main";
    }

    @GetMapping("/public/property/{slug}")
    public String propertyDetail(@PathVariable("slug") String slug, Model model) {
        Post post = postService.getMarketplacePostBySlug(slug)
                .orElseThrow(() -> new com.pms.propertymanagement.exception.ResourceNotFoundException("Post not found or not available"));

        postService.incrementView(slug);

        PostPublicResponse p = PostPublicResponse.from(post);
        model.addAttribute("p", p);

        model.addAttribute("content", "public/property-detail");
        model.addAttribute("contact", new ContactRequest());

        return "layout/public-main";
    }

}
