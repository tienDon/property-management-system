package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.ContactInquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactInquiryRepository extends JpaRepository<ContactInquiry, Long> {
    //Tim inquiry theo roomId
    List<ContactInquiry> findByRoomIdOrderByCreatedAtDesc(Long roomId);
    // Tìm inquiry do sender gửi (tenant)
    List<ContactInquiry> findBySenderIdOrderByCreatedAtDesc(Long senderId);

    // Tìm inquiry gửi đến receiver (host)
    List<ContactInquiry> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    // Tìm inquiry theo status
    List<ContactInquiry> findByStatusOrderByCreatedAtDesc(String status);

    // Tìm inquiry của host theo status
    List<ContactInquiry> findByReceiverIdAndStatusOrderByCreatedAtDesc(
            Long receiverId, String status);

    // Đếm số inquiry PENDING của host
    @Query("SELECT COUNT(c) FROM ContactInquiry c " +
            "WHERE c.receiver.id = :hostId AND c.status = 'PENDING'")
    Long countPendingByHost(@Param("hostId") Long hostId);
}
