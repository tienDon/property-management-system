package com.pms.propertymanagement.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.regex.Pattern;

@UtilityClass
public class FileUpLoadUtil {
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    public static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp");
    public static final String DATE_FORMAT = "yyyyMMddHHmmss";

    public static final String FILE_NAME_FORMAT = "%s_%s_%s";

    /**
     * Kiểm tra file name có đúng extension cho phép không
     */
    public static boolean isAllowedExtension(String fileName, String pattern) {
        return Pattern
                .compile(pattern, Pattern.CASE_INSENSITIVE)
                .matcher(fileName)
                .matches();
    }

    public static void assertAllowedImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không hợp lệ");
        }

        final long size = file.getSize();
        if (size > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Max file size is 10MB");
        }

        final String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Thiếu tên file");
        }

        final String extension = FilenameUtils.getExtension(fileName);
        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException("File không có phần mở rộng");
        }

        final String normalizedExtension = extension.toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(normalizedExtension)) {
            throw new IllegalArgumentException("Only jpg, jpeg, png, gif, bmp files are supported");
        }
    }
    public static String getFileName(final String name) {
        final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        final String date = dateFormat.format(System.currentTimeMillis());
        return String.format(FILE_NAME_FORMAT, name, date);
    }

}
