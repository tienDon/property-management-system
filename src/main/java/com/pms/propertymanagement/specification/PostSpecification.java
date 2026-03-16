package com.pms.propertymanagement.specification;

import com.pms.propertymanagement.dto.request.PostFilterDTO;
import com.pms.propertymanagement.entity.Amenity;
import com.pms.propertymanagement.entity.Post;
import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.enums.PostStatus;
import com.pms.propertymanagement.enums.PropertyStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostSpecification {

    public static Specification<Post> filter(PostFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Post, Property> propertyJoin = root.join("property");

            // 1. Status: ACTIVE and Non-expired
            predicates.add(cb.equal(root.get("status"), PostStatus.ACTIVE));
            predicates.add(cb.greaterThan(root.get("postExpiredAt"), LocalDateTime.now()));
            // Also ensure Property is PUBLISHED
            predicates.add(propertyJoin.get("status").in(PropertyStatus.PUBLISHED, PropertyStatus.ACTIVE));

            // 2. Keyword (Title or Description or Address)
            if (StringUtils.hasText(filter.getKeyword())) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), keyword);
                Predicate descLike = cb.like(cb.lower(root.get("description")), keyword);
                Predicate addressLike = cb.like(cb.lower(propertyJoin.get("addressNumber")), keyword);
                predicates.add(cb.or(titleLike, descLike, addressLike));
            }

            // 3. Category
            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(propertyJoin.get("category").get("id"), filter.getCategoryId()));
            }

            // 4. Location
            if (StringUtils.hasText(filter.getWardCode())) {
                predicates.add(cb.equal(propertyJoin.get("ward").get("code"), filter.getWardCode()));
            } else if (StringUtils.hasText(filter.getProvinceCode())) {
                predicates.add(cb.equal(propertyJoin.get("ward").get("province").get("code"), filter.getProvinceCode()));
            }

            // 5. Price
            if (filter.getPriceMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(propertyJoin.get("price"), filter.getPriceMin()));
            }
            if (filter.getPriceMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(propertyJoin.get("price"), filter.getPriceMax()));
            }

            // 6. Area
            if (filter.getAreaMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(propertyJoin.get("acreage"), filter.getAreaMin()));
            }
            if (filter.getAreaMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(propertyJoin.get("acreage"), filter.getAreaMax()));
            }

            // 7. Rooms
            if (filter.getRoomMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(propertyJoin.get("numberOfRooms"), filter.getRoomMin()));
            }
            if (filter.getRoomMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(propertyJoin.get("numberOfRooms"), filter.getRoomMax()));
            }

            // 8. Amenities (Must have ALL selected)
            if (filter.getAmenityIds() != null && !filter.getAmenityIds().isEmpty()) {
                for (Long amenityId : filter.getAmenityIds()) {
                    Subquery<Long> sub = query.subquery(Long.class);
                    Root<Property> subRoot = sub.from(Property.class);
                    Join<Property, Amenity> subAmenities = subRoot.join("amenities");
                    sub.select(subRoot.get("id"));
                    sub.where(cb.equal(subAmenities.get("id"), amenityId), cb.equal(subRoot, propertyJoin));
                    predicates.add(cb.exists(sub));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
