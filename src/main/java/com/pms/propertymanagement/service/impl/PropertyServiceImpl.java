package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.request.PropertyRequest;
import com.pms.propertymanagement.dto.response.IconResponse;
import com.pms.propertymanagement.dto.response.PropertyDetailResponse;
import com.pms.propertymanagement.dto.response.PropertyOwnerResponse;
import com.pms.propertymanagement.dto.response.PropertyResponse;
import com.pms.propertymanagement.enums.RoomStatus;
import com.pms.propertymanagement.entity.*;
import com.pms.propertymanagement.exception.ResourceNotFoundException;
import com.pms.propertymanagement.repository.*;
import com.pms.propertymanagement.service.PropertyService;
import com.pms.propertymanagement.utils.DateUtil;
import com.pms.propertymanagement.utils.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService
{
    private final PropertyRepository propertyRepository;
    private final CategoryRepository categoryRepository;
    private final WardRepository wardRepository;
    private final AmenityRepository amenityRepository;
    private final SurroundingRepository surroundingRepository;
    private final TargetTenantsRepository targetRepository;
    private final ProvinceRepository provinceRepository;

    @Override
    public List<Amenity> getAllAmenities() {
        return amenityRepository.findAll();
    }

    @Override
    public List<Surrounding> getAllSurroundings() {
        return surroundingRepository.findAll();
    }

    @Override
    public List<TargetTenant> getAllTargetTenants() {
        return targetRepository.findAll();
    }

    @Override
    public List<Province> getAllProvinces() {
        return provinceRepository.findAll();
    }

    @Override
    public List<PropertyOwnerResponse> getPropertiesByOwner(User owner) {
        List<Property> properties = propertyRepository.findByOwnerUsername(owner.getUsername());

        return properties.stream()
                .map(p -> { // p ở đây là Entity Property
                    PropertyOwnerResponse dto = new PropertyOwnerResponse();

                    // Lấy dữ liệu từ p (Entity) và set vào dto
                    dto.setId(p.getId());
                    dto.setName(p.getName()); // Map tên nhà trọ
                    dto.setTitle(p.getTitle());
                    dto.setAddressNumber(p.getAddressNumber());

                    // Gọi getCategory() từ p chứ không phải từ dto
                    if (p.getCategory() != null) {
                        dto.setCategoryName(p.getCategory().getName());
                    }

                    // Gọi getCreatedAt() từ p để format ngày tháng
                    if (p.getCreatedAt() != null) {
                        dto.setFormattedCreatedAt(DateUtil.formatDateTime(p.getCreatedAt()));
                    }

                    // Gọi getImages() từ p để lấy ảnh đại diện
                    if (p.getImages() != null && !p.getImages().isEmpty()) {
                        dto.setImg_url(p.getImages().get(0).getImageUrl());
                    }

                    // Tính số lượng phòng
                    dto.setTotalRooms(p.getNumberOfRooms());
                    
                    int rentedCount = 0;
                    if (p.getRooms() != null) {
                        rentedCount = (int) p.getRooms().stream()
                                .filter(r -> r.getStatus() == RoomStatus.RENTED)
                                .count();
                    }
                    dto.setRentedRooms(rentedCount);

                    return dto;
                })
                .toList();
    }

    @Override
    public void createProperty(PropertyRequest request, User owner) {
        Property property = new Property();

        property.setName(request.getName());
        property.setTitle(request.getTitle());
        property.setNumberOfRooms(request.getNumberOfRooms());
        property.setAcreage(request.getAcreage());
        property.setAddressNumber(request.getAddressNumber());
        property.setDescription(request.getDescription());
        property.setOwner(owner);

        property.setSlug(SlugUtil.makeSlug(request.getTitle()));

        categoryRepository.findById(request.getCategoryId())
                .ifPresent(property::setCategory);

        wardRepository.findById(request.getWardCode())
                .ifPresent(property::setWard);

        if (request.getAmenityIds() != null) {
            property.setAmenities(new HashSet<>(amenityRepository.findAllById(request.getAmenityIds())));
        }
        if (request.getSurroundingIds() != null) {
            property.setSurroundings(new HashSet<>(surroundingRepository.findAllById(request.getSurroundingIds())));
        }
        if (request.getTargetIds() != null) {
            property.setTargetTenants(new HashSet<>(targetRepository.findAllById(request.getTargetIds())));
        }

        // ĐẶT GIÁ TRỊ PRICE LẤY TỪ REQUEST
        property.setPrice(request.getPrice());

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<PropertyImage> images = new ArrayList<>();

            for (int i = 0; i < request.getImageUrls().size(); i++) {
                PropertyImage img = new PropertyImage();

                // 1. Gán đúng tên field trong Entity của bạn là imageUrl
                img.setImageUrl(request.getImageUrls().get(i));

                // 2. Xử lý field isPrimary: Ảnh đầu tiên (index = 0) là ảnh chính
                img.setIsPrimary(i == 0);

                // 3. Thiết lập quan hệ ngược lại với Property
                img.setProperty(property);

                images.add(img);
            }

            property.setImages(images);
        }

        propertyRepository.save(property);
    }

    @Override
    public PropertyRequest getPropertyForEdit(Long id) {
        Property p = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        
        PropertyRequest req = new PropertyRequest();
        req.setName(p.getName());
        req.setTitle(p.getTitle());
        req.setNumberOfRooms(p.getNumberOfRooms());
        req.setPrice(p.getPrice());
        req.setAcreage(p.getAcreage());
        req.setAddressNumber(p.getAddressNumber());
        req.setDescription(p.getDescription());
        
        if (p.getCategory() != null) req.setCategoryId(p.getCategory().getId());
        if (p.getWard() != null) req.setWardCode(p.getWard().getCode());
        
        req.setAmenityIds(p.getAmenities().stream().map(Amenity::getId).toList());
        req.setSurroundingIds(p.getSurroundings().stream().map(Surrounding::getId).toList());
        req.setTargetIds(p.getTargetTenants().stream().map(TargetTenant::getId).toList());
        
        // Populate image URLs if needed for display in edit form
        if (p.getImages() != null) {
            req.setImageUrls(p.getImages().stream().map(PropertyImage::getImageUrl).toList());
        }
        
        return req;
    }

    @Override
    public void updateProperty(Long id, PropertyRequest request) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
                
        property.setName(request.getName());
        property.setTitle(request.getTitle());
        property.setNumberOfRooms(request.getNumberOfRooms());
        property.setPrice(request.getPrice());
        property.setAcreage(request.getAcreage());
        property.setAddressNumber(request.getAddressNumber());
        property.setDescription(request.getDescription());
        
        categoryRepository.findById(request.getCategoryId()).ifPresent(property::setCategory);
        wardRepository.findById(request.getWardCode()).ifPresent(property::setWard);
        
        if (request.getAmenityIds() != null) {
            property.setAmenities(new HashSet<>(amenityRepository.findAllById(request.getAmenityIds())));
        }
        if (request.getSurroundingIds() != null) {
            property.setSurroundings(new HashSet<>(surroundingRepository.findAllById(request.getSurroundingIds())));
        }
        if (request.getTargetIds() != null) {
            property.setTargetTenants(new HashSet<>(targetRepository.findAllById(request.getTargetIds())));
        }
        
        // Basic Image Update Strategy: Append new ones or Replace? 
        // For simplicity, let's just append new non-empty URLs if this is a simple text input list
        // Real implementation usually involves checking existing IDs or clearing and re-adding.
        // Given the request just has Strings (URLs), we might clear and re-add if the UI sends ALL urls.
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
             // If the UI sends the full list of desired URLs, we can replace:
             property.getImages().clear();
             for (int i = 0; i < request.getImageUrls().size(); i++) {
                String url = request.getImageUrls().get(i);
                if (url != null && !url.trim().isEmpty()) {
                    PropertyImage img = new PropertyImage();
                    img.setImageUrl(url);
                    img.setIsPrimary(i == 0);
                    img.setProperty(property);
                    property.getImages().add(img);
                }
            }
        }
        
        propertyRepository.save(property);
    }

    @Override
    public void deleteProperty(Long id) {
        propertyRepository.deleteById(id);
    }

    @Override
    public List<PropertyResponse> getPropertiesByCategory(Long categoryId) {
        List<Property> properties = propertyRepository.findByCategory_Id(categoryId);

        return properties.stream().map(p -> PropertyResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .price(p.getPrice()) // Đảm bảo trong Entity Property đã có trường price
                .categoryName(p.getCategory().getName())
                .acreage(p.getAcreage())
                .wardName(p.getWard().getName())
                .provinceName(p.getWard().getProvince().getName())
                .slug(p.getSlug())
                // Lấy ảnh đầu tiên hoặc ảnh isPrimary
                .imageUrl(p.getImages().isEmpty() ? "/images/default.jpg" : p.getImages().getFirst().getImageUrl())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public PropertyDetailResponse getPropertyDetailBySlug(String slug) {
        Property p = propertyRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng!"));

        return PropertyDetailResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .slug(p.getSlug())
                .price(p.getPrice())
                .description(p.getDescription())
                .addressNumber(p.getAddressNumber())
                .wardName(p.getWard().getName())
                .provinceName(p.getWard().getProvince().getName())
                .ownerName(p.getOwner().getFullName()) // Chạy mượt vì đã FETCH owner
                .ownerPhone(p.getOwner().getPhone())
                .imageUrls(p.getImages().stream().map(PropertyImage::getImageUrl).toList())
                .amenities(p.getAmenities().stream().map(a -> new IconResponse(a.getName(), a.getIcon())).toList())
                .surroundings(p.getSurroundings().stream().map(s -> new IconResponse(s.getName(), s.getIcon())).toList())
                .targetTenants(p.getTargetTenants().stream().map(t -> new IconResponse(t.getName(), t.getIcon())).toList())
                .formattedCreatedAt(DateUtil.formatDateTime(p.getCreatedAt()))
                .categoryName(p.getCategory().getName())
                .acreage(p.getAcreage())
                .numberOfRooms(p.getNumberOfRooms())
                .build();
    }

    @Override
    public User getOwnerByPropertySlug(String propertySlug) {
        return propertyRepository.findBySlug(propertySlug).map(Property::getOwner).orElse(null);
    }
}
