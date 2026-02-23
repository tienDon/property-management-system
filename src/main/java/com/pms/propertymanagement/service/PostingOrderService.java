package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.PostingOrder;
import com.pms.propertymanagement.entity.Property;

public interface PostingOrderService {

    PostingOrder createNewOrderForDefaultPackage(Long ownerId);
    
    PostingOrder createOrderForPackage(Long ownerId, Long packageId);

    PostingOrder getOrderForOwner(Long orderId, Long ownerId);

    String createVnpayPaymentUrl(Long orderId, Long ownerId, String ipAddress, String returnUrl);

    boolean canPost(Long ownerId);

    //Trừ 1 lượt khi tạo nhà trọ mới + insert posting_usages
    void consumeOneUseForNewProperty(Long ownerId, Property property);
}