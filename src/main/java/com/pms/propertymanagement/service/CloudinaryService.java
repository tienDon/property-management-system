package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.response.CloudinaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface CloudinaryService {
    CloudinaryResponse uploadImage(MultipartFile file);

    CloudinaryResponse uploadImage(MultipartFile file, String module);

    CloudinaryResponse uploadImage(File file);

    CloudinaryResponse uploadImage(File file, String module);

    void deleteImage(String publicId);
}
