package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.request.*;
import com.pms.propertymanagement.dto.response.*;
import com.pms.propertymanagement.entity.EkycSubmission;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.repository.EkycSubmissionRepository;
import com.pms.propertymanagement.repository.UploadFileRepository;
import com.pms.propertymanagement.repository.UserRepository;
import com.pms.propertymanagement.service.AiService;
import com.pms.propertymanagement.service.FileUploadService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class EkycController {

    private final AiService aiService;
    private final FileUploadService fileUploadService;
    private final UserRepository userRepository;
    private final EkycSubmissionRepository ekycSubmissionRepository;
    private final UploadFileRepository uploadFileRepository;
    
    @Value("${vnpt.unit}")
    private String vnptUnit;

    @Value("${ekyc.persist-vnpt-hash:true}")
    private boolean persistVnptHash;

    @Value("${ekyc.purge-uploadfile-after-ekyc:false}")
    private boolean purgeUploadFileAfterEkyc;

    private static final String CLIENT_SESSION = "ANDROID_nokia7.2_28_Simulator_2.4.2_08d2d8686ee5fa0e_1581910116532";
    private static final String TOKEN = "123456"; // Dummy token
    private static final String CROP_PARAM = "0.14,0.3";
    private static final String REDACTED_HASH = "REDACTED";

    @GetMapping("/ekyc")
    public String showEkyc(@RequestParam(value = "next", required = false) String next,
                           HttpSession session,
                           Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        User user = userRepository.findById(sessionUser.getId()).orElse(sessionUser);
        session.setAttribute("user", user);

        if (!requiresEkyc(user)) {
            return "redirect:" + safeNextOrDefault(next, user);
        }
        if (user.isEkycVerified()) {
            if (user.getAccountType() == null) {
                return "redirect:/ekyc/choose-role?next=" + safeNextOrDefault(next, user);
            }
            return "redirect:" + safeNextOrDefault(next, user);
        }

        model.addAttribute("next", safeNextOrDefault(next, user));
        ekycSubmissionRepository.findTopByUserOrderByCreatedAtDesc(user)
                .ifPresent(s -> model.addAttribute("lastSubmissionAt", s.getCreatedAt()));
        return "public/ekyc";
    }

    @PostMapping("/ekyc")
    public String submitEkyc(@RequestParam("frontImage") MultipartFile frontImage,
                             @RequestParam("backImage") MultipartFile backImage,
                             @RequestParam("faceImage") MultipartFile faceImage,
                             @RequestParam(value = "next", required = false) String next,
                             HttpSession session,
                             Model model) throws Exception {
        System.out.println("--- Starting EKYC Submission ---");
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        if (user == null) {
            session.invalidate();
            return "redirect:/login";
        }
        if (!requiresEkyc(user)) {
            return "redirect:" + safeNextOrDefault(next, user);
        }

        List<String> errors = new ArrayList<>();

        String frontHash = null;
        String backHash = null;
        String faceHash = null;
        UploadImageResult frontUpload = null;
        UploadImageResult backUpload = null;
        UploadImageResult faceUpload = null;

        try {
            System.out.println("Uploading front image...");
            frontUpload = fileUploadService.uploadToVNPTWithCloudinary(frontImage, "cccd front");
            frontHash = frontUpload.getHash();
            System.out.println("Front hash: " + frontHash);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error uploading front image: " + e.getMessage());
        }

        try {
            System.out.println("Uploading back image...");
            backUpload = fileUploadService.uploadToVNPTWithCloudinary(backImage, "cccd back");
            backHash = backUpload.getHash();
            System.out.println("Back hash: " + backHash);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error uploading back image: " + e.getMessage());
        }

        try {
            System.out.println("Uploading face image...");
            faceUpload = fileUploadService.uploadToVNPTWithCloudinary(faceImage, "face");
            faceHash = faceUpload.getHash();
            System.out.println("Face hash: " + faceHash);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error uploading face image: " + e.getMessage());
        }

        if (frontHash == null || frontHash.isBlank()) errors.add("Upload CCCD mặt trước thất bại");
        if (backHash == null || backHash.isBlank()) errors.add("Upload CCCD mặt sau thất bại");
        if (faceHash == null || faceHash.isBlank()) errors.add("Upload ảnh khuôn mặt thất bại");

        if (!errors.isEmpty()) {
            System.out.println("Upload failed. Errors: " + errors);
            model.addAttribute("errors", errors);
            model.addAttribute("next", safeNextOrDefault(next, user));
            return "public/ekyc";
        }

        // 1. OCR Check
        System.out.println("Calling OCR ID API...");
        OcrIdRequest ocrRequest = new OcrIdRequest();
        ocrRequest.setImgFront(frontHash);
        ocrRequest.setImgBack(backHash);
        ocrRequest.setClientSession(CLIENT_SESSION);
        ocrRequest.setType(-1);
        ocrRequest.setCropParam(CROP_PARAM);
        ocrRequest.setValidatePostcode(true);
        ocrRequest.setToken(TOKEN);

        OcrIdResponse ocrResponse = null;
        try {
            ocrResponse = aiService.ocrId(ocrRequest);
            System.out.println("OCR Response: " + ocrResponse);
            if (!"IDG-00000000".equals(ocrResponse.getMessage()) || !"OK".equals(ocrResponse.getResult().getMsg())) {
                errors.add("Không thể đọc thông tin từ giấy tờ. Vui lòng chụp lại rõ nét hơn.");
                System.out.println("OCR Logic Error: " + ocrResponse.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            errors.add("Lỗi khi đọc thông tin giấy tờ: " + e.getMessage());
            System.err.println("OCR Exception: " + e.getMessage());
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return "public/ekyc";
        }

        // 2. Face Liveness Check
        System.out.println("Calling Face Liveness API...");
        FaceLivenessRequest livenessRequest = new FaceLivenessRequest();
        livenessRequest.setImg(faceHash);
        livenessRequest.setClientSession(CLIENT_SESSION);
        livenessRequest.setToken(TOKEN);

        try {
            FaceLivenessResponse livenessResponse = aiService.faceLiveness(livenessRequest);
            System.out.println("Liveness Response: " + livenessResponse);
            if (!"IDG-00000000".equals(livenessResponse.getMessage()) || 
                !"success".equalsIgnoreCase(livenessResponse.getResult().getLiveness())) {
                errors.add("Ảnh khuôn mặt không hợp lệ (Không phải người thật).");
            }
        } catch (Exception e) {
             e.printStackTrace();
             errors.add("Lỗi khi kiểm tra khuôn mặt: " + e.getMessage());
             System.err.println("Liveness Exception: " + e.getMessage());
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return "public/ekyc";
        }

        // 3. Face Compare
        System.out.println("Calling Face Compare API...");
        FaceCompareRequest compareRequest = new FaceCompareRequest();
        compareRequest.setImgFront(frontHash);
        compareRequest.setImgFace(faceHash);
        compareRequest.setClientSession(CLIENT_SESSION);
        compareRequest.setToken(TOKEN);

        try {
            FaceCompareResponse compareResponse = aiService.faceCompare(compareRequest);
            System.out.println("Compare Response: " + compareResponse);
            if (!"IDG-00000000".equals(compareResponse.getMessage()) || 
                !"MATCH".equalsIgnoreCase(compareResponse.getResult().getMsg())) {
                errors.add("Khuôn mặt trên giấy tờ không khớp với ảnh chụp chân dung.");
            }
        } catch (Exception e) {
             e.printStackTrace();
             errors.add("Lỗi khi so sánh khuôn mặt: " + e.getMessage());
             System.err.println("Compare Exception: " + e.getMessage());
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return "public/ekyc";
        }

        // 4. Save User Info & Mark Verified
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
        session.setAttribute("user", saved);

        // 5. Save Submission Record
        String frontHashForStore = persistVnptHash ? frontHash : REDACTED_HASH;
        String backHashForStore = persistVnptHash ? backHash : REDACTED_HASH;
        String faceHashForStore = persistVnptHash ? faceHash : REDACTED_HASH;

        EkycSubmission submission = new EkycSubmission();
        submission.setUser(user);
        submission.setFrontHash(frontHashForStore);
        submission.setBackHash(backHashForStore);
        submission.setFaceHash(faceHashForStore);
        if (frontUpload != null) {
            submission.setFrontCloudinaryUrl(frontUpload.getCloudinaryUrl());
            submission.setFrontCloudinaryPublicId(frontUpload.getCloudinaryPublicId());
        }
        if (backUpload != null) {
            submission.setBackCloudinaryUrl(backUpload.getCloudinaryUrl());
            submission.setBackCloudinaryPublicId(backUpload.getCloudinaryPublicId());
        }
        if (faceUpload != null) {
            submission.setFaceCloudinaryUrl(faceUpload.getCloudinaryUrl());
            submission.setFaceCloudinaryPublicId(faceUpload.getCloudinaryPublicId());
        }
        ekycSubmissionRepository.save(submission);

        // 6. Add Face to System (Async or just try-catch)
        try {
            FaceAddRequest faceAddRequest = new FaceAddRequest();
            faceAddRequest.setBbox(null); // Optional?
            faceAddRequest.setLandmark(null); // Optional?
            faceAddRequest.setUnit(vnptUnit); // Use configured unit
            
            FaceAddRequest.CustomerInformation customerInfo = new FaceAddRequest.CustomerInformation();
            customerInfo.setFullname(ocrResult.getName());
            customerInfo.setDob(ocrResult.getBirthDay());
            customerInfo.setCardId(ocrResult.getId());
            customerInfo.setIpfs(faceHash);
            customerInfo.setNationality(ocrResult.getNationality());
            customerInfo.setHometown(ocrResult.getOriginLocation());
            customerInfo.setAddress(ocrResult.getRecentLocation());
            customerInfo.setGender(ocrResult.getGender());
            // Set other required fields with dummy or extracted data
            customerInfo.setPassportId("");
            customerInfo.setDriverLicenseId("");
            customerInfo.setMilitaryId("");
            customerInfo.setPoliceId("");
            customerInfo.setOtherId("");
            customerInfo.setOtherType("OTHER");
            
            faceAddRequest.setCustomerInformation(customerInfo);
            
            aiService.faceAdd(faceAddRequest);
        } catch (Exception e) {
            // Log error but don't fail the registration flow
            System.err.println("Failed to add face to system: " + e.getMessage());
        }

        if (purgeUploadFileAfterEkyc) {
            try {
                uploadFileRepository.deleteByHash(frontHash);
            } catch (Exception ignored) { }
            try {
                uploadFileRepository.deleteByHash(backHash);
            } catch (Exception ignored) { }
            try {
                uploadFileRepository.deleteByHash(faceHash);
            } catch (Exception ignored) { }
        }

        return "redirect:/ekyc/choose-role?next=" + safeNextOrDefault(next, saved);
    }

    private boolean requiresEkyc(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals("USER") || r.getName().equals("OWNER"));
    }

    private String safeNextOrDefault(String next, User user) {
        if (next != null && isValidRedirectUrl(next)) return next;
        if (user.getRoles().stream().anyMatch(r -> r.getName().equals("OWNER"))) return "/owner";
        return "/tenant/rooms";
    }

    private boolean isValidRedirectUrl(String url) {
        return url.startsWith("/") && !url.startsWith("//");
    }
}
