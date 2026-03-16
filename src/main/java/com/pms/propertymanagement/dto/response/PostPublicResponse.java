package com.pms.propertymanagement.dto.response;

import com.pms.propertymanagement.entity.*;
import com.pms.propertymanagement.utils.DateUtil;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Flattened view of Post + Property used by all public-facing Thymeleaf templates.
 * Replaces passing raw Post / Property entities to the view layer.
 */
@Getter
@Builder
public class PostPublicResponse {

    // ── Post fields ────────────────────────────────────────────────────────
    private Long id;            // Post ID
    private String title;       // Marketing title (from Post)
    private String slug;        // URL slug  (from Post)
    private String description; // Marketing description (from Post)
    private String formattedCreatedAt;

    // ── Property core fields ───────────────────────────────────────────────
    private int price;
    private double acreage;
    private int numberOfRooms;
    private String addressNumber;

    // ── Location ───────────────────────────────────────────────────────────
    private Long categoryId;
    private String categoryName;
    private String wardName;
    private String provinceName;

    // ── Images ────────────────────────────────────────────────────────────
    private String imageUrl;        // thumbnail (first image)
    private List<String> imageUrls; // all images for gallery

    // ── Owner ─────────────────────────────────────────────────────────────
    private Long ownerId;
    private String ownerName;
    private String ownerPhone;

    // ── Tags / Feature sets ───────────────────────────────────────────────
    private Set<Amenity>       amenities;
    private Set<Surrounding>   surroundings;
    private Set<TargetTenant>  targetTenants;

    // ── Factory ───────────────────────────────────────────────────────────

    public static PostPublicResponse from(Post post) {
        Property prop = post.getProperty();

        List<String> urls = (prop.getImages() != null)
                ? prop.getImages().stream().map(PropertyImage::getImageUrl).collect(Collectors.toList())
                : Collections.emptyList();

        String thumbnail = urls.isEmpty() ? "/images/no-image.jpg" : urls.get(0);

        String wardName     = (prop.getWard() != null) ? prop.getWard().getName() : "";
        String provinceName = (prop.getWard() != null && prop.getWard().getProvince() != null)
                ? prop.getWard().getProvince().getName() : "";

        String categoryName = (prop.getCategory() != null) ? prop.getCategory().getName() : "";
        Long   categoryId   = (prop.getCategory() != null) ? prop.getCategory().getId()   : null;

        String ownerName  = (prop.getOwner() != null) ? prop.getOwner().getFullName() : "";
        String ownerPhone = (prop.getOwner() != null) ? prop.getOwner().getPhone()    : "";
        Long ownerId = (prop.getOwner() != null) ? prop.getOwner().getId() : null;

        return PostPublicResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .description(post.getDescription())
                .formattedCreatedAt(post.getCreatedAt() != null ? DateUtil.formatDateTime(post.getCreatedAt()) : "")
                .price(prop.getPrice())
                .acreage(prop.getAcreage())
                .numberOfRooms(prop.getNumberOfRooms())
                .addressNumber(prop.getAddressNumber())
                .categoryId(categoryId)
                .categoryName(categoryName)
                .wardName(wardName)
                .provinceName(provinceName)
                .imageUrl(thumbnail)
                .imageUrls(urls)
                .ownerId(ownerId)
                .ownerName(ownerName)
                .ownerPhone(ownerPhone)
                .amenities(prop.getAmenities())
                .surroundings(prop.getSurroundings())
                .targetTenants(prop.getTargetTenants())
                .build();
    }
}
