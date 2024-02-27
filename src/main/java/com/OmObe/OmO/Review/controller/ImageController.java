package com.OmObe.OmO.Review.controller;

import com.OmObe.OmO.Review.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j
@Validated
@RequestMapping("/image")
public class ImageController {
    private final ReviewService reviewService;

    public ImageController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{image-name}")
    public ResponseEntity getImage(@PathVariable("image-name") String imageName) throws IOException {
        byte[] imageData = reviewService.downloadImageFromFileSystem(imageName);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.IMAGE_PNG)
                .body(imageData);
    }
}
