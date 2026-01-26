package com.pms.propertymanagement.dto.request;

import com.pms.propertymanagement.entity.PropertyImage;
import com.pms.propertymanagement.entity.Room;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PropertyRequest {

    private String name;
    private String title;
    private String addressNumber;
    private String ward_code;
    private String description;
    private String categoryId;
    //Vĩ độ
    private String latitude;

    //Kinh độ
    private String longitude;

    private Long owner_id;
    private List<Room> rooms;
    private List<PropertyImage> images;

}
