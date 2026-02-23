package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.PostingOrder;
import com.pms.propertymanagement.enums.PaymentStatus;
import com.pms.propertymanagement.enums.TransactionType;
import com.pms.propertymanagement.repository.PostingOrderRepository;
import com.pms.propertymanagement.service.VnPayService;
import com.pms.propertymanagement.service.WalletService;
import com.pms.propertymanagement.utils.VnPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class VnPayServiceImpl implements VnPayService {

    private final PostingOrderRepository postingOrderRepository;
    private final WalletService walletService;
    
    public VnPayServiceImpl(PostingOrderRepository postingOrderRepository, @Lazy WalletService walletService) {
        this.postingOrderRepository = postingOrderRepository;
        this.walletService = walletService;
    }

    @Value("${vnpay.tmnCode}")
    private String tmnCode;

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    @Value("${vnpay.payUrl}")
    private String payUrl;

    // vẫn giữ default trong properties (fallback)
    @Value("${vnpay.returnUrl}")
    private String returnUrl;

    private static final DateTimeFormatter VN_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public String createPaymentUrl(PostingOrder order, String ipAddress, String returnUrlOverride) {
        long amount = (long) order.getAmount() * 100; // VNPay amount = VND * 100

        // TxnRef unique
        String txnRef = order.getVnpTxnRef();
        if (txnRef == null || txnRef.isBlank()) {
            txnRef = "ORDER" + order.getId() + "_" + System.currentTimeMillis();
            order.setVnpTxnRef(txnRef);
            postingOrderRepository.save(order);
        }

        LocalDateTime now = LocalDateTime.now();
        String createDate = now.format(VN_DATE);
        String expireDate = now.plusMinutes(15).format(VN_DATE);

        // returnUrl động theo domain hiện tại
        String finalReturnUrl = (returnUrlOverride != null && !returnUrlOverride.isBlank())
                ? returnUrlOverride
                : returnUrl;

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Thanh toan goi dang tin - Order#" + order.getId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", finalReturnUrl);
        params.put("vnp_IpAddr", (ipAddress == null || ipAddress.isBlank()) ? "127.0.0.1" : ipAddress);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        String hashData = VnPayUtil.buildQueryString(params, true);
        String secureHash = VnPayUtil.hmacSHA512(hashSecret, hashData);

        String queryString = VnPayUtil.buildQueryString(params, false);
        return payUrl + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public String createWalletDepositUrl(Long amount, String txnRef, String returnUrl, String ipAddress) {
        long vnpayAmount = amount * 100; // VNPay amount = VND * 100
        
        LocalDateTime now = LocalDateTime.now();
        String createDate = now.format(VN_DATE);
        String expireDate = now.plusMinutes(15).format(VN_DATE);
        
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(vnpayAmount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Nap tien vao vi - " + amount + " VND");
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", (ipAddress == null || ipAddress.isBlank()) ? "127.0.0.1" : ipAddress);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);
        
        String hashData = VnPayUtil.buildQueryString(params, true);
        String secureHash = VnPayUtil.hmacSHA512(hashSecret, hashData);
        
        String queryString = VnPayUtil.buildQueryString(params, false);
        return payUrl + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public VnPayReturnResult handleReturn(Map<String, String> returnParams, String rawQuery) {
        boolean validSig = VnPayUtil.verifySecureHash(returnParams, hashSecret);
        if (!validSig) {
            return new VnPayReturnResult(false, false, "Chữ ký không hợp lệ (SecureHash).", null);
        }

        String txnRef = returnParams.get("vnp_TxnRef");
        if (txnRef == null || txnRef.isBlank()) {
            return new VnPayReturnResult(true, false, "Thiếu vnp_TxnRef.", null);
        }

        PostingOrder order = postingOrderRepository.findByVnpTxnRef(txnRef).orElse(null);
        if (order == null) {
            return new VnPayReturnResult(true, false, "Không tìm thấy đơn hàng theo vnp_TxnRef.", null);
        }

        String responseCode = returnParams.getOrDefault("vnp_ResponseCode", "");
        String transactionNo = returnParams.getOrDefault("vnp_TransactionNo", "");
        String bankCode = returnParams.getOrDefault("vnp_BankCode", "");
        String payDate = returnParams.getOrDefault("vnp_PayDate", "");
        String vnpAmountStr = returnParams.getOrDefault("vnp_Amount", "0");

        // lưu raw fields
        order.setVnpResponseCode(responseCode);
        order.setVnpTransactionNo(transactionNo);
        order.setVnpBankCode(bankCode);
        order.setVnpPayDate(payDate);

        if (rawQuery != null) {
            order.setVnpRawQuery(rawQuery.length() <= 2000 ? rawQuery : rawQuery.substring(0, 2000));
        }

        // validate amount
        long expected = (long) order.getAmount() * 100;
        long received;
        try {
            received = Long.parseLong(vnpAmountStr);
        } catch (NumberFormatException e) {
            received = -1;
        }

        if (received != expected) {
            order.setStatus(PaymentStatus.FAILED);
            postingOrderRepository.save(order);
            return new VnPayReturnResult(true, false, "Sai số tiền thanh toán.", order.getId());
        }

        // success = "00"
        if ("00".equals(responseCode)) {
            if (order.getStatus() != PaymentStatus.PAID) {
                // Trừ tiền từ ví và tạo wallet transaction
                try {
                    walletService.deduct(
                        order.getOwner(), 
                        BigDecimal.valueOf(order.getAmount()), 
                        TransactionType.PURCHASE,
                        "Mua gói đăng tin: " + order.getPostingPackage().getName(),
                        "ORDER_" + order.getId()
                    );
                    
                    // Cập nhật order status
                    order.setStatus(PaymentStatus.PAID);
                    order.setPaidAt(LocalDateTime.now());
                    order.setRemainingUses(order.getPostingPackage().getUsageLimit());
                } catch (Exception e) {
                    // Nếu không đủ tiền ví thì fail payment
                    order.setStatus(PaymentStatus.FAILED);
                    postingOrderRepository.save(order);
                    return new VnPayReturnResult(true, false, "Không đủ số dư trong ví để thanh toán. " + e.getMessage(), order.getId());
                }
            }
            postingOrderRepository.save(order);
            return new VnPayReturnResult(true, true, "Thanh toán thành công! Số tiền đã được trừ khỏi ví của bạn.", order.getId());
        }

        order.setStatus(PaymentStatus.FAILED);
        postingOrderRepository.save(order);
        return new VnPayReturnResult(true, false, "Thanh toán thất bại. Mã: " + responseCode, order.getId());
    }
}