package com.pms.propertymanagement.dto.request;

import com.pms.propertymanagement.entity.PropertyImage;
import com.pms.propertymanagement.entity.Room;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class PropertyRequest {

    private String name;
    private String title;
    private int numberOfRooms;
    private int price;
    private double acreage;
    private String addressNumber;
    private String description;

    // Các ID để mapping quan hệ
    private Long categoryId;
    private String wardCode;

    // Danh sách ID từ checkbox (Many-to-Many)
    private List<Long> amenityIds;
    private List<Long> surroundingIds;
    private List<Long> targetIds;

//    private List<MultipartFile> imageFiles;
    private List<String> imageUrls;
}
