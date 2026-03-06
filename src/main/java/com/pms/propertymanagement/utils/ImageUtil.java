package com.pms.propertymanagement.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public class ImageUtil {
    public static File resizeImage(MultipartFile multipartFile) throws IOException {
        // Hoàn toàn không resize, giữ nguyên 100% bản gốc bất kể dung lượng
        File tempFile = File.createTempFile("original_", multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);
        return tempFile;
    }

}
