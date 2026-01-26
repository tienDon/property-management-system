package com.pms.propertymanagement.mapper;

import com.pms.propertymanagement.dto.response.RoomSearchResponse;
import com.pms.propertymanagement.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    // ✅ CHỈ MAP FIELD CÓ CHẮC TRONG Room
    @Mapping(target = "id", source = "id")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "area", source = "area")
    @Mapping(target = "maxPeople", source = "maxPeople")

    // ❗ các field dưới TẠM THỜI bỏ trống (set null)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "provinceName", ignore = true)
    @Mapping(target = "districtName", ignore = true)
    @Mapping(target = "wardName", ignore = true)
    @Mapping(target = "addressNumber", ignore = true)
    @Mapping(target = "primaryImageUrl", ignore = true)

    RoomSearchResponse toRoomSearchResponse(Room room);

    List<RoomSearchResponse> toRoomSearchResponseList(List<Room> rooms);
}
