package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.request.ContactRequest;
import com.pms.propertymanagement.dto.response.ContactResponse;
import com.pms.propertymanagement.entity.Contact;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.repository.ContactRepository;
import com.pms.propertymanagement.repository.PropertyRepository;
import com.pms.propertymanagement.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final PropertyRepository propertyRepository;

    @Override
    public List<ContactResponse> getContactsByOwner(Long ownerId) {
        return contactRepository.findByOwnerId(ownerId).stream().map(
                c -> new ContactResponse(
                        c.getId(), 
                        c.getName(), 
                        c.getPhone(), 
                        c.getNote(), 
                        c.getProperty() != null ? c.getProperty().getTitle() : "N/A",
                        c.getProperty() != null ? c.getProperty().getSlug() : "#",
                        c.getIsChecked()
                ))
                .toList();
    }

    @Override
    public void createContact(String propertySlug, ContactRequest contactRequest) {
        var property = propertyRepository.findBySlug(propertySlug)
                .orElseThrow(() -> new com.pms.propertymanagement.exception.ResourceNotFoundException("Property not found"));
        
        contactRepository.save(new Contact(
                contactRequest.getName(), 
                contactRequest.getPhone(), 
                contactRequest.getNote(), 
                property.getOwner(),
                property
        ));
    }

    @Override
    public void changeStatus(Long contactId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new com.pms.propertymanagement.exception.ResourceNotFoundException("Contact not found"));
        contact.setIsChecked(!contact.getIsChecked());
        contactRepository.save(contact);
    }
}
