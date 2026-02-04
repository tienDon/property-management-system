package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.request.ContactRequest;
import com.pms.propertymanagement.dto.response.ContactResponse;
import com.pms.propertymanagement.entity.User;

import java.util.List;

public interface ContactService {
    List<ContactResponse> getContactsByOwner(Long ownerId);

    void createContact(String propertySlug, ContactRequest contactRequest);

    void changeStatus(Long contactId);
}
