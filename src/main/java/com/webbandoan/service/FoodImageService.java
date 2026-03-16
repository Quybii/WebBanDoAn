package com.webbandoan.service;

import com.webbandoan.entity.Food;
import com.webbandoan.entity.FoodImage;
import com.webbandoan.repository.FoodImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FoodImageService {

    private final FoodImageRepository foodImageRepository;
    private final Path uploadRoot;

    public FoodImageService(FoodImageRepository foodImageRepository,
                            @Value("${file.upload-dir:uploads}") String uploadDir) {
        this.foodImageRepository = foodImageRepository;
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Transactional
    public List<FoodImage> storeImages(Food food, MultipartFile[] files) {
        List<FoodImage> saved = new ArrayList<>();
        if (files == null) return saved;
        Path foodDir = uploadRoot.resolve("foods").resolve(String.valueOf(food.getId()));
        try {
            Files.createDirectories(foodDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create food upload dir", e);
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String ext = "";
            String name = file.getOriginalFilename();
            if (name != null && name.contains(".")) ext = name.substring(name.lastIndexOf('.'));
            String filename = System.currentTimeMillis() + "-" + UUID.randomUUID() + ext;
            Path target = foodDir.resolve(filename);
            try {
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                continue;
            }
            String webPath = "/uploads/foods/" + food.getId() + "/" + filename;
            FoodImage fi = new FoodImage(food, webPath, false);
            saved.add(foodImageRepository.save(fi));
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<FoodImage> findByFood(Food food) {
        return foodImageRepository.findByFoodOrderByIdAsc(food);
    }

    @Transactional
    public void deleteImage(Long imageId) {
        if (imageId != null) {
            foodImageRepository.findById(imageId).ifPresent(img -> {
                String url = img.getImageUrl();
                if (url != null && url.startsWith("/uploads/")) {
                    Path p = uploadRoot.resolve(url.replaceFirst("/uploads/", ""));
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                }
                foodImageRepository.delete(img);
            });
        }
    }

    @Transactional
    public void setMain(Long imageId) {
        if (imageId != null) {
            foodImageRepository.findById(imageId).ifPresent(img -> {
                Food food = img.getFood();
                List<FoodImage> imgs = foodImageRepository.findByFoodOrderByIdAsc(food);
                if (imgs != null) {
                    for (FoodImage f : imgs) {
                        f.setIsMain(f.getId().equals(imageId));
                    }
                    foodImageRepository.saveAll(imgs);
                }
            });
        }
    }
}
