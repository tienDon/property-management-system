package com.pms.propertymanagement.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.pms.propertymanagement.dto.response.CloudinaryResponse;
import com.pms.propertymanagement.service.CloudinaryService;
import com.pms.propertymanagement.utils.FileUpLoadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;
    private final String defaultFolder;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public CloudinaryServiceImpl(
            Cloudinary cloudinary,
            @Value("${cloudinary.folder:}") String defaultFolder,
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {
        this.cloudinary = cloudinary;
        this.defaultFolder = defaultFolder;
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @Override
    public CloudinaryResponse uploadImage(MultipartFile file) {
        return uploadImage(file, null);
    }

    @Override
    public CloudinaryResponse uploadImage(MultipartFile file, String module) {
        assertConfigured();
        FileUpLoadUtil.assertAllowedImage(file);
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("resource_type", "image");
            String folder = buildFolder(module);
            if (folder != null && !folder.isBlank()) {
                options.put("folder", folder);
            }

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    options);
            String publicId = String.valueOf(result.get("public_id"));
            Object secureUrl = result.get("secure_url");
            String url = secureUrl != null ? String.valueOf(secureUrl) : String.valueOf(result.get("url"));

            return CloudinaryResponse.builder()
                    .publicId(publicId)
                    .url(url)
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Upload Cloudinary thất bại", e);
        }
    }

    private String buildFolder(String module) {
        String normalizedModule = normalizeModule(module);
        if (defaultFolder == null || defaultFolder.isBlank()) {
            return normalizedModule;
        }
        if (normalizedModule == null || normalizedModule.isBlank()) {
            return defaultFolder;
        }
        return defaultFolder + "/" + normalizedModule;
    }

    private String normalizeModule(String module) {
        if (module == null || module.isBlank()) {
            return null;
        }
        String normalized = module.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        if ("reports".equals(normalized)
                || "report".equals(normalized)
                || "waste_report".equals(normalized)
                || "waste_reports".equals(normalized)
                || "report_image".equals(normalized)
                || "report_images".equals(normalized)) {
            return "reports";
        }
        if ("requests".equals(normalized)) {
            return "requests";
        }
        if ("maintenance".equals(normalized)
                || "maintenance_request".equals(normalized)
                || "maintenance_requests".equals(normalized)) {
            return "maintenance";
        }
        if ("feedbacks".equals(normalized)) {
            return "feedbacks";
        }
        if ("collector_reports".equals(normalized)
                || "collector_report".equals(normalized)
                || "collectorreport".equals(normalized)
                || "collectorreports".equals(normalized)
                || "colletor_reports".equals(normalized)
                || "colletor_report".equals(normalized)
                || "colletorreport".equals(normalized)
                || "colletorreports".equals(normalized)) {
            return "collectorReport";
        }
        throw new IllegalArgumentException(
                "module không hợp lệ (chỉ chấp nhận: reports, requests, feedbacks, collectorReport)");
    }

    @Override
    public void deleteImage(String publicId) {
        assertConfigured();
        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException("publicId không hợp lệ");
        }
        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", "image", "invalidate", true));
        } catch (IOException e) {
            throw new IllegalStateException("Xóa Cloudinary thất bại", e);
        }
    }

    private void assertConfigured() {
        if (cloudName == null || cloudName.isBlank()
                || apiKey == null || apiKey.isBlank()
                || apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalStateException(
                    "Thiếu cấu hình Cloudinary (CLOUDINARY_CLOUD_NAME / CLOUDINARY_API_KEY / CLOUDINARY_API_SECRET)");
        }
    }
}
