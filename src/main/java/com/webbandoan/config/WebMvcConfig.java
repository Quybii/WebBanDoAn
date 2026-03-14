package com.webbandoan.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String uploadPath;

    public WebMvcConfig(@Value("${file.upload-dir:uploads}") String uploadDir) {
        Path p = Path.of(uploadDir).toAbsolutePath().normalize();
        this.uploadPath = p.toUri().toString();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // serve /uploads/** from local filesystem uploadDir
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
