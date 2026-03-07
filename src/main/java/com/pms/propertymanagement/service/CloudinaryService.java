package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.response.CloudinaryResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    CloudinaryResponse uploadImage(MultipartFile file);

    CloudinaryResponse uploadImage(MultipartFile file, String module);

    void deleteImage(String publicId);
}
