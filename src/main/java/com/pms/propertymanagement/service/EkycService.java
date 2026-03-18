package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.ekyc.EkycPageOutcome;
import com.pms.propertymanagement.dto.ekyc.EkycSubmitOutcome;
import com.pms.propertymanagement.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EkycService {
    Optional<User> refreshUser(Long userId);
    boolean requiresEkyc(User user);
    String safeNextOrDefault(String next, User user);
    Optional<LocalDateTime> getLastSubmissionAt(User user);
    EkycPageOutcome buildPageOutcome(User user, String next);
    EkycSubmitOutcome submit(User user, MultipartFile frontImage, MultipartFile backImage, MultipartFile faceImage, String next);
}
