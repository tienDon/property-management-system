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
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PublicController {

    private final CategoryService categoryService;
    private final PropertyService propertyService;
    private final PostService postService;

    @GetMapping("/")
    public String index(Model model) {
        List<PostPublicResponse> properties = postService.getAllMarketplacePosts()
                .stream().map(PostPublicResponse::from).toList();

        addCommonAttributes(model);
        model.addAttribute("pageTitle", "Tim phong tro, can ho mini va nha nguyen can");
        model.addAttribute("content", "public/home");
        model.addAttribute("properties", properties);
        model.addAttribute("featuredProperty", properties.isEmpty() ? null : properties.get(0));
        model.addAttribute("latestProperties", properties.stream().limit(6).toList());
        model.addAttribute("postCount", properties.size());
        model.addAttribute(
                "provinceCount",
                properties.stream()
                        .map(PostPublicResponse::getProvinceName)
                        .filter(name -> name != null && !name.isBlank())
                        .distinct()
                        .count()
        );
        model.addAttribute(
                "startingPrice",
                properties.stream()
                        .mapToInt(PostPublicResponse::getPrice)
                        .filter(price -> price > 0)
                        .min()
                        .orElse(0)
        );
        model.addAttribute(
                "featuredLocations",
                properties.stream()
                        .map(p -> p.getWardName() + ", " + p.getProvinceName())
                        .filter(location -> !location.startsWith(","))
                        .distinct()
                        .limit(4)
                        .collect(Collectors.toList())
        );

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
        addCommonAttributes(model);
        model.addAttribute("pageTitle", "Danh sach phong tro va nha cho thue");

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
        addCommonAttributes(model);
        model.addAttribute("pageTitle", p.getTitle());

        model.addAttribute("content", "public/property-detail");
        model.addAttribute("contact", new ContactRequest());

        return "layout/public-main";
    }

    @GetMapping("/video-review")
    public String videoReview(Model model) {
        List<PostPublicResponse> properties = postService.getAllMarketplacePosts()
                .stream().map(PostPublicResponse::from).toList();

        addCommonAttributes(model);
        model.addAttribute("pageTitle", "Video review phong tro va can ho mini");
        model.addAttribute("content", "public/video-review");
        model.addAttribute("videoHighlights", properties.stream().limit(3).toList());
        model.addAttribute("featuredProperty", properties.isEmpty() ? null : properties.get(0));

        return "layout/public-main";
    }

    private void addCommonAttributes(Model model) {
        model.addAttribute("categories", categoryService.findAll());
    }

}
