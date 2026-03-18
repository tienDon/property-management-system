package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.ekyc.EkycPageOutcome;
import com.pms.propertymanagement.dto.ekyc.EkycSubmitOutcome;
import com.pms.propertymanagement.dto.request.FaceAddRequest;
import com.pms.propertymanagement.dto.request.FaceCompareRequest;
import com.pms.propertymanagement.dto.request.FaceLivenessRequest;
import com.pms.propertymanagement.dto.request.OcrIdRequest;
import com.pms.propertymanagement.dto.response.FaceCompareResponse;
import com.pms.propertymanagement.dto.response.FaceLivenessResponse;
import com.pms.propertymanagement.dto.response.OcrIdResponse;
import com.pms.propertymanagement.entity.EkycSubmission;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.repository.EkycSubmissionRepository;
import com.pms.propertymanagement.repository.UserRepository;
import com.pms.propertymanagement.service.AiService;
import com.pms.propertymanagement.service.EkycService;
import com.pms.propertymanagement.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EkycServiceImpl implements EkycService {

    private final AiService aiService;
    private final FileUploadService fileUploadService;
    private final UserRepository userRepository;
    private final EkycSubmissionRepository ekycSubmissionRepository;

    @Value("${vnpt.unit}")
    private String vnptUnit;

    @Value("${vnpt.ekyc.client-session:ANDROID_nokia7.2_28_Simulator_2.4.2_08d2d8686ee5fa0e_1581910116532}")
    private String clientSession;

    @Value("${vnpt.ekyc.token:123456}")
    private String token;

    @Value("${vnpt.ekyc.crop-param:0.14,0.3}")
    private String cropParam;

    @Value("${vnpt.ekyc.ocr-type:-1}")
    private Integer ocrType;

    @Override
    public Optional<User> refreshUser(Long userId) {
        if (userId == null) return Optional.empty();
        return userRepository.findById(userId);
    }

    @Override
    public boolean requiresEkyc(User user) {
        if (user == null || user.getRoles() == null) return false;
        return user.getRoles().stream().anyMatch(r -> "USER".equals(r.getName()) || "OWNER".equals(r.getName()));
    }

    @Override
    public String safeNextOrDefault(String next, User user) {
        if (next != null && isValidRedirectUrl(next)) return next;
        if (user != null && user.getRoles() != null && user.getRoles().stream().anyMatch(r -> "OWNER".equals(r.getName()))) {
            return "/owner";
        }
        return "/tenant/rooms";
    }

    @Override
    public Optional<LocalDateTime> getLastSubmissionAt(User user) {
        if (user == null) return Optional.empty();
        return ekycSubmissionRepository.findTopByUserOrderByCreatedAtDesc(user).map(EkycSubmission::getCreatedAt);
    }

    @Override
    public EkycPageOutcome buildPageOutcome(User user, String next) {
        EkycPageOutcome outcome = new EkycPageOutcome();
        String safeNext = safeNextOrDefault(next, user);
        outcome.setNext(safeNext);

        if (user == null) {
            outcome.setRedirect(true);
            outcome.setRedirectUrl("/login");
            return outcome;
        }

        if (!requiresEkyc(user) || user.isEkycVerified()) {
            outcome.setRedirect(true);
            outcome.setRedirectUrl(safeNext);
            return outcome;
        }

        getLastSubmissionAt(user).ifPresent(outcome::setLastSubmissionAt);
        outcome.setRedirect(false);
        return outcome;
    }

    @Override
    public EkycSubmitOutcome submit(User user, MultipartFile frontImage, MultipartFile backImage, MultipartFile faceImage, String next) {
        EkycSubmitOutcome outcome = new EkycSubmitOutcome();
        outcome.setNext(safeNextOrDefault(next, user));

        if (user == null) {
            outcome.setSuccess(false);
            outcome.setErrors(List.of("Phiên đăng nhập không hợp lệ"));
            return outcome;
        }

        if (!requiresEkyc(user)) {
            outcome.setSuccess(true);
            outcome.setRedirectUrl(outcome.getNext());
            outcome.setUser(user);
            return outcome;
        }

        List<String> errors = new ArrayList<>();

        String frontHash = uploadOrAddError(frontImage, "cccd front", "Upload CCCD mặt trước thất bại", errors);
        String backHash = uploadOrAddError(backImage, "cccd back", "Upload CCCD mặt sau thất bại", errors);
        String faceHash = uploadOrAddError(faceImage, "face", "Upload ảnh khuôn mặt thất bại", errors);

        if (!errors.isEmpty()) {
            outcome.setSuccess(false);
            outcome.setErrors(errors);
            return outcome;
        }

        OcrIdResponse ocrResponse = callOcr(frontHash, backHash, errors);
        if (!errors.isEmpty()) {
            outcome.setSuccess(false);
            outcome.setErrors(errors);
            return outcome;
        }

        callFaceLiveness(faceHash, errors);
        if (!errors.isEmpty()) {
            outcome.setSuccess(false);
            outcome.setErrors(errors);
            return outcome;
        }

        callFaceCompare(frontHash, faceHash, errors);
        if (!errors.isEmpty()) {
            outcome.setSuccess(false);
            outcome.setErrors(errors);
            return outcome;
        }

        OcrIdResponse.OcrIdResult ocrResult = ocrResponse.getResult();
        user.setFullName(ocrResult.getName());
        user.setCardId(ocrResult.getId());
        user.setDob(ocrResult.getBirthDay());
        user.setAddress(ocrResult.getRecentLocation());
        user.setHometown(ocrResult.getOriginLocation());
        user.setGender(ocrResult.getGender());
        user.setEkycVerified(true);
        user.setEkycVerifiedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        EkycSubmission submission = new EkycSubmission();
        submission.setUser(saved);
        submission.setFrontHash(frontHash);
        submission.setBackHash(backHash);
        submission.setFaceHash(faceHash);
        ekycSubmissionRepository.save(submission);

        tryAddFace(ocrResult, faceHash);

        outcome.setSuccess(true);
        outcome.setRedirectUrl(outcome.getNext());
        outcome.setUser(saved);
        return outcome;
    }

    private String uploadOrAddError(MultipartFile file, String title, String errorMessage, List<String> errors) {
        try {
            String hash = fileUploadService.uploadToVNPT(file, title);
            if (hash == null || hash.isBlank()) {
                errors.add(errorMessage);
                return null;
            }
            return hash;
        } catch (Exception e) {
            log.warn("VNPT upload failed: {}", title, e);
            errors.add(errorMessage);
            return null;
        }
    }

    private OcrIdResponse callOcr(String frontHash, String backHash, List<String> errors) {
        OcrIdRequest request = new OcrIdRequest();
        request.setImgFront(frontHash);
        request.setImgBack(backHash);
        request.setClientSession(clientSession);
        request.setType(ocrType);
        request.setCropParam(cropParam);
        request.setValidatePostcode(true);
        request.setToken(token);

        try {
            OcrIdResponse response = aiService.ocrId(request);
            if (!isOcrOk(response)) {
                errors.add("Không thể đọc thông tin từ giấy tờ. Vui lòng chụp lại rõ nét hơn.");
            }
            return response;
        } catch (Exception e) {
            log.warn("OCR failed", e);
            errors.add("Lỗi khi đọc thông tin giấy tờ");
            return null;
        }
    }

    private void callFaceLiveness(String faceHash, List<String> errors) {
        FaceLivenessRequest request = new FaceLivenessRequest();
        request.setImg(faceHash);
        request.setClientSession(clientSession);
        request.setToken(token);

        try {
            FaceLivenessResponse response = aiService.faceLiveness(request);
            if (!isLivenessOk(response)) {
                errors.add("Ảnh khuôn mặt không hợp lệ (Không phải người thật).");
            }
        } catch (Exception e) {
            log.warn("Face liveness failed", e);
            errors.add("Lỗi khi kiểm tra khuôn mặt");
        }
    }

    private void callFaceCompare(String frontHash, String faceHash, List<String> errors) {
        FaceCompareRequest request = new FaceCompareRequest();
        request.setImgFront(frontHash);
        request.setImgFace(faceHash);
        request.setClientSession(clientSession);
        request.setToken(token);

        try {
            FaceCompareResponse response = aiService.faceCompare(request);
            if (!isCompareOk(response)) {
                errors.add("Khuôn mặt trên giấy tờ không khớp với ảnh chụp chân dung.");
            }
        } catch (Exception e) {
            log.warn("Face compare failed", e);
            errors.add("Lỗi khi so sánh khuôn mặt");
        }
    }

    private void tryAddFace(OcrIdResponse.OcrIdResult ocrResult, String faceHash) {
        try {
            FaceAddRequest faceAddRequest = new FaceAddRequest();
            faceAddRequest.setBbox(null);
            faceAddRequest.setLandmark(null);
            faceAddRequest.setUnit(vnptUnit);

            FaceAddRequest.CustomerInformation customerInfo = new FaceAddRequest.CustomerInformation();
            customerInfo.setFullname(ocrResult.getName());
            customerInfo.setDob(ocrResult.getBirthDay());
            customerInfo.setCardId(ocrResult.getId());
            customerInfo.setIpfs(faceHash);
            customerInfo.setNationality(ocrResult.getNationality());
            customerInfo.setHometown(ocrResult.getOriginLocation());
            customerInfo.setAddress(ocrResult.getRecentLocation());
            customerInfo.setGender(ocrResult.getGender());
            customerInfo.setPassportId("");
            customerInfo.setDriverLicenseId("");
            customerInfo.setMilitaryId("");
            customerInfo.setPoliceId("");
            customerInfo.setOtherId("");
            customerInfo.setOtherType("OTHER");

            faceAddRequest.setCustomerInformation(customerInfo);
            aiService.faceAdd(faceAddRequest);
        } catch (Exception e) {
            log.warn("Face add failed", e);
        }
    }

    private boolean isOcrOk(OcrIdResponse response) {
        if (response == null || response.getResult() == null) return false;
        return "IDG-00000000".equals(response.getMessage()) && "OK".equals(response.getResult().getMsg());
    }

    private boolean isLivenessOk(FaceLivenessResponse response) {
        if (response == null || response.getResult() == null) return false;
        return "IDG-00000000".equals(response.getMessage()) && "success".equalsIgnoreCase(response.getResult().getLiveness());
    }

    private boolean isCompareOk(FaceCompareResponse response) {
        if (response == null || response.getResult() == null) return false;
        return "IDG-00000000".equals(response.getMessage()) && "MATCH".equalsIgnoreCase(response.getResult().getMsg());
    }

    private boolean isValidRedirectUrl(String url) {
        return url.startsWith("/") && !url.startsWith("//");
    }
}
