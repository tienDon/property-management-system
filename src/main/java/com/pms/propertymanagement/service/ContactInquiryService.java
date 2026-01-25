package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.ContactInquiry;

import java.util.List;

public interface ContactInquiryService {
    ContactInquiry createInquiry(ContactInquiry inquiry);

    ContactInquiry updateStatus(Long id, String status);

    List<ContactInquiry> getInquiriesBySender(Long senderId);

    List<ContactInquiry> getInquiriesByReceiver(Long receiverId);

    List<ContactInquiry> getPendingInquiries(Long receiverId);

    Long countPendingByHost(Long hostId);

    ContactInquiry getById(Long id);

    List<ContactInquiry> getAll();
}
