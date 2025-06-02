package com.example.travelweb.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileUploadService {

    // Thư mục lưu trữ file upload nằm trong thư mục static/images
    private final Path tourImagesPath;
    private final Path categoryImagesPath;

    public FileUploadService() {
        // Đường dẫn tương đối đến thư mục lưu trữ ảnh
        this.tourImagesPath = Paths.get("src/main/resources/static/images/tours");
        this.categoryImagesPath = Paths.get("src/main/resources/static/images/categories");
        
        // Đảm bảo thư mục tồn tại
        try {
            if (!Files.exists(tourImagesPath)) {
                Files.createDirectories(tourImagesPath);
            }
            if (!Files.exists(categoryImagesPath)) {
                Files.createDirectories(categoryImagesPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục lưu trữ ảnh", e);
        }
    }

    /**
     * Lưu file ảnh tour vào thư mục static với tên định dạng tour-XXX.jpg
     * 
     * @param file File ảnh được upload
     * @param tourId ID của tour
     * @return Đường dẫn tương đối đến file ảnh (để lưu vào database)
     * @throws IOException Nếu có lỗi khi lưu file
     */
    public String saveTourImage(MultipartFile file, Long tourId) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File ảnh trống");
        }

        // Định dạng ID tour thành chuỗi 3 chữ số (001, 002, ...)
        String formattedId = String.format("%03d", tourId);
        String fileName = "tour-" + formattedId + getFileExtension(file.getOriginalFilename());
        
        // Lưu file
        Path targetLocation = tourImagesPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        // Trả về đường dẫn tương đối để lưu vào database
        return "/images/tours/" + fileName;
    }

    /**
     * Lưu file ảnh danh mục vào thư mục static với tên định dạng category-XXX.png
     * 
     * @param file File ảnh được upload
     * @param categoryId ID của danh mục
     * @return Đường dẫn tương đối đến file ảnh (để lưu vào database)
     * @throws IOException Nếu có lỗi khi lưu file
     */
    public String saveCategoryIcon(MultipartFile file, Long categoryId) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File ảnh trống");
        }

        // Định dạng ID danh mục thành chuỗi 3 chữ số (001, 002, ...)
        String formattedId = String.format("%03d", categoryId);
        String fileName = "category-" + formattedId + getFileExtension(file.getOriginalFilename());
        
        // Lưu file
        Path targetLocation = categoryImagesPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        // Trả về đường dẫn tương đối để lưu vào database
        return "/images/categories/" + fileName;
    }
    
    /**
     * Lấy phần mở rộng của file (ví dụ: .jpg, .png)
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return ".jpg"; // Mặc định là .jpg nếu không có phần mở rộng
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
} 