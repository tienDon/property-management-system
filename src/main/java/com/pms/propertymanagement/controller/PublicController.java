package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.request.ContactRequest;
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
        List<Post> posts = postService.getAllMarketplacePosts();
        
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("content", "public/home");
        model.addAttribute("posts", posts);

        return "layout/public-main";
    }

    @GetMapping("/public/category/{id}")
    public String listByCategory(@PathVariable("id") Long categoryId, Model model) {
        // NEW ARCHITECTURE: Get marketplace posts by category
        List<Post> posts = postService.getMarketplacePostsByCategory(categoryId);

        model.addAttribute("posts", posts);
        model.addAttribute("currentCategoryId", categoryId);

        // Vẫn cần các list phụ cho sidebar/search
        model.addAttribute("allAmenities", propertyService.getAllAmenities());
        model.addAttribute("categoryId", categoryId);

        model.addAttribute("content", "public/property-list");
        return "layout/public-main";
    }

    @GetMapping("/public/property/{slug}")
    public String propertyDetail(@PathVariable("slug") String slug, Model model) {
        // NEW ARCHITECTURE: Get Post by slug (marketing content + property data)
        Post post = postService.getMarketplacePostBySlug(slug)
                .orElseThrow(() -> new com.pms.propertymanagement.exception.ResourceNotFoundException("Post not found or not available"));
        
        // Increment view count
        postService.incrementView(slug);

        // Pass both post (marketing) and property (real estate) to template
        model.addAttribute("post", post);
        model.addAttribute("p", post.getProperty()); // For backward compatibility with templates

        model.addAttribute("content", "public/property-detail");
        model.addAttribute("contact", new ContactRequest());

        return "layout/public-main";
    }

}
