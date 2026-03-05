package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.request.ContactRequest;
import com.pms.propertymanagement.dto.response.ContactResponse;
import com.pms.propertymanagement.entity.Contact;
import com.pms.propertymanagement.entity.Post;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.repository.ContactRepository;
import com.pms.propertymanagement.repository.PostRepository;
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
    private final PostRepository postRepository;

    @Override
    public List<ContactResponse> getContactsByOwner(Long ownerId) {
        return contactRepository.findByOwnerId(ownerId).stream().map(
                c -> {
                    String title = "N/A";
                    String slug = "#";
                    
                    // NEW ARCHITECTURE: Get title/slug from Post, not Property
                    if (c.getProperty() != null) {
                        Post post = postRepository.findByPropertyId(c.getProperty().getId()).orElse(null);
                        if (post != null) {
                            title = post.getTitle();
                            slug = post.getSlug();
                        }
                    }
                    
                    return new ContactResponse(
                            c.getId(), 
                            c.getName(), 
                            c.getPhone(), 
                            c.getNote(), 
                            title,
                            slug,
                            c.getIsChecked()
                    );
                })
                .toList();
    }

    @Override
    public void createContact(String propertySlug, ContactRequest contactRequest) {
        // NEW ARCHITECTURE: Slug belongs to Post, not Property
        Post post = postRepository.findBySlug(propertySlug)
                .orElseThrow(() -> new com.pms.propertymanagement.exception.ResourceNotFoundException("Post not found"));
        
        var property = post.getProperty();
        if (property == null) {
            throw new com.pms.propertymanagement.exception.ResourceNotFoundException("Property not found for this post");
        }
        
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
