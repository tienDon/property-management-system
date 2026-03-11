package com.pms.propertymanagement.utils;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtil {

    public static File resizeImage(MultipartFile multipartFile) throws IOException {
        // Đọc ảnh từ MultipartFile
        BufferedImage originalImage = ImageIO.read(multipartFile.getInputStream());
        if (originalImage == null) {
            // Trường hợp file không phải ảnh đọc được, trả về file gốc
            File tempFile = File.createTempFile("original_", multipartFile.getOriginalFilename());
            multipartFile.transferTo(tempFile);
            return tempFile;
        }

        // Kiểm tra kích thước, nếu nhỏ hơn hoặc bằng 1600px chiều rộng thì không cần resize
        int targetWidth = 1600;
        if (originalImage.getWidth() <= targetWidth) {
            File tempFile = File.createTempFile("original_", multipartFile.getOriginalFilename());
            multipartFile.transferTo(tempFile);
            return tempFile;
        }

        // Tính toán chiều cao mới để giữ nguyên tỷ lệ
        int targetHeight = (int) (originalImage.getHeight() * ((double) targetWidth / originalImage.getWidth()));

        // Tạo ảnh mới với kích thước đã resize
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resizedImage.createGraphics();
        
        // Cấu hình chất lượng resize tốt nhất
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();

        // Lưu ảnh đã resize vào file tạm (luôn lưu là JPG để tối ưu cho OCR)
        File tempFile = File.createTempFile("resized_", ".jpg");
        ImageIO.write(resizedImage, "jpg", tempFile);
        
        return tempFile;
    }
}
