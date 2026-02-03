package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.PostingOrder;
import com.pms.propertymanagement.entity.PostingPackage;
import com.pms.propertymanagement.entity.PostingUsage;
import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.PaymentStatus;
import com.pms.propertymanagement.repository.PostingOrderRepository;
import com.pms.propertymanagement.repository.PostingUsageRepository;
import com.pms.propertymanagement.service.PostingOrderService;
import com.pms.propertymanagement.service.PostingPackageService;
import com.pms.propertymanagement.service.UserService;
import com.pms.propertymanagement.service.VnPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostingOrderServiceImpl implements PostingOrderService {

    private final PostingOrderRepository postingOrderRepository;
    private final PostingUsageRepository postingUsageRepository;
    private final PostingPackageService postingPackageService;
    private final VnPayService vnPayService;
    private final UserService userService;

    @Override
    @Transactional
    public PostingOrder createNewOrderForDefaultPackage(Long ownerId) {
        User owner = userService.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy owner id=" + ownerId));

        PostingPackage pkg = postingPackageService.getDefaultPostingPackage();

        PostingOrder order = new PostingOrder();
        order.setOwner(owner);
        order.setPostingPackage(pkg);
        order.setAmount(pkg.getPrice());
        order.setStatus(PaymentStatus.PENDING);
        order.setRemainingUses(0);

        return postingOrderRepository.save(order);
    }

    @Override
    public PostingOrder getOrderForOwner(Long orderId, Long ownerId) {
        PostingOrder order = postingOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy order id=" + orderId));

        if (order.getOwner() == null || !order.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập order này.");
        }
        return order;
    }

    @Override
    @Transactional
    public String createVnpayPaymentUrl(Long orderId, Long ownerId, String ipAddress, String returnUrl) {
        PostingOrder order = getOrderForOwner(orderId, ownerId);

        if (order.getStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("Order đã thanh toán rồi.");
        }

        return vnPayService.createPaymentUrl (order, ipAddress, returnUrl);
    }

    @Override
    public boolean canPost(Long ownerId) {
        return postingOrderRepository.existsByOwner_IdAndStatusAndRemainingUsesGreaterThan(
                ownerId,
                PaymentStatus.PAID,
                0
        );
    }

    @Override
    @Transactional
    public void consumeOneUseForNewProperty(Long ownerId, Property property) {
        PostingOrder order = postingOrderRepository
                .findTopByOwner_IdAndStatusAndRemainingUsesGreaterThanOrderByPaidAtDesc(
                        ownerId, PaymentStatus.PAID, 0
                )
                .orElseThrow(() -> new IllegalStateException("Bạn phải mua gói đăng tin mới."));

        // trừ 1 lượt
        order.setRemainingUses(order.getRemainingUses() - 1);
        postingOrderRepository.save(order);

        // insert usage log
        PostingUsage usage = new PostingUsage();
        usage.setOrder(order);
        usage.setProperty(property);
        postingUsageRepository.save(usage);
    }
}
