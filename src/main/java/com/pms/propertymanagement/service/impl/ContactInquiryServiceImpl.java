package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.ContactInquiry;
import com.pms.propertymanagement.repository.ContactInquiryRepository;
import com.pms.propertymanagement.service.ContactInquiryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContactInquiryServiceImpl implements ContactInquiryService {

    private final ContactInquiryRepository inquiryRepository;

    @Override
    public ContactInquiry createInquiry(ContactInquiry inquiry) {
        return inquiryRepository.save(inquiry);
    }

    @Override
    public ContactInquiry updateStatus(Long id, String status) {
        ContactInquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));
        inquiry.setStatus(status);
        return inquiryRepository.save(inquiry);
    }

    @Override
    public List<ContactInquiry> getInquiriesBySender(Long senderId) {
        return inquiryRepository.findBySenderIdOrderByCreatedAtDesc(senderId);
    }

    @Override
    public List<ContactInquiry> getInquiriesByReceiver(Long receiverId) {
        return inquiryRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);
    }

    @Override
    public List<ContactInquiry> getPendingInquiries(Long receiverId) {
        return inquiryRepository.findByReceiverIdAndStatusOrderByCreatedAtDesc(
                receiverId, "PENDING");
    }

    @Override
    public Long countPendingByHost(Long hostId) {
        return inquiryRepository.countPendingByHost(hostId);
    }

    @Override
    public ContactInquiry getById(Long id) {
        return inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));
    }

    @Override
    public List<ContactInquiry> getAll() {
        return inquiryRepository.findAll();
    }
}
