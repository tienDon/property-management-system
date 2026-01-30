package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.dto.request.ContactRequest;
import com.pms.propertymanagement.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact,Long>
{
    List<Contact> findByOwnerId(Long ownerId);
}
