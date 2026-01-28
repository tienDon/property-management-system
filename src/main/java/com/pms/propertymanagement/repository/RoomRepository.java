package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    @Query("SELECT r FROM Room r WHERE r.property.owner.username = :username")
    List<Room> findAllByOwnerUsername(String username);

    List<Room> findByProperty_Id(Long propertyId);
    
    List<Room> findByPropertyIdAndStatus(Long propertyId, RoomStatus status);
}
