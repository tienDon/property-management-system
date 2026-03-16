package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.response.UploadImageResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploadService {
    String uploadToVNPT(MultipartFile file, String title) throws IOException;

    UploadImageResult uploadToVNPTWithCloudinary(MultipartFile file, String title) throws IOException;
}
