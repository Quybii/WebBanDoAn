package com.webbandoan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Class chính để chạy ứng dụng Spring Boot.
 * Chứa method main() - điểm vào duy nhất.
 */
@SpringBootApplication
public class WebBanDoAnApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebBanDoAnApplication.class, args);
    }
}
