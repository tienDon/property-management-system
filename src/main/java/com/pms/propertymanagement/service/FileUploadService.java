package com.pms.propertymanagement.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploadService {
    String uploadToVNPT(MultipartFile file, String title) throws IOException;
}
