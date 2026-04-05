package com.webbandoan.service;

import com.webbandoan.entity.FoodReview;
import com.webbandoan.entity.FoodReviewImage;
import com.webbandoan.repository.FoodReviewImageRepository;
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
public class FoodReviewImageService {

    private final FoodReviewImageRepository foodReviewImageRepository;
    private final Path uploadRoot;

    public FoodReviewImageService(FoodReviewImageRepository foodReviewImageRepository,
                                  @Value("${file.upload-dir:uploads}") String uploadDir) {
        this.foodReviewImageRepository = foodReviewImageRepository;
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Transactional
    public List<FoodReviewImage> storeImages(FoodReview review, MultipartFile[] files) {
        List<FoodReviewImage> saved = new ArrayList<>();
        if (review == null || files == null || files.length == 0) {
            return saved;
        }

        Path reviewDir = uploadRoot.resolve("reviews").resolve(String.valueOf(review.getId()));
        try {
            Files.createDirectories(reviewDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create review upload dir", e);
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String ext = "";
            String name = file.getOriginalFilename();
            if (name != null && name.contains(".")) {
                ext = name.substring(name.lastIndexOf('.'));
            }
            String filename = System.currentTimeMillis() + "-" + UUID.randomUUID() + ext;
            Path target = reviewDir.resolve(filename);
            try {
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                continue;
            }
            String webPath = "/uploads/reviews/" + review.getId() + "/" + filename;
            saved.add(foodReviewImageRepository.save(new FoodReviewImage(review, webPath)));
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<FoodReviewImage> findByReview(FoodReview review) {
        return foodReviewImageRepository.findByReviewOrderByIdAsc(review);
    }

    @Transactional
    public void deleteImagesByIds(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }

        for (Long imageId : imageIds) {
            if (imageId == null) {
                continue;
            }
            foodReviewImageRepository.findById(imageId).ifPresent(img -> {
                String url = img.getImageUrl();
                if (url != null && url.startsWith("/uploads/")) {
                    Path p = uploadRoot.resolve(url.replaceFirst("/uploads/", ""));
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                }
                foodReviewImageRepository.delete(img);
            });
        }
    }

    @Transactional
    public void deleteByReview(FoodReview review) {
        if (review == null) {
            return;
        }
        deleteImagesByIds(foodReviewImageRepository.findByReviewOrderByIdAsc(review)
                .stream()
                .map(FoodReviewImage::getId)
                .toList());
    }
}