package com.OmObe.OmO.ImageManager;

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
    private final ImageManager imageManager;

    public ImageController(ImageManager imageManager) {
        this.imageManager = imageManager;
    }

    // 이미지명(확장자 포함)을 입력하면 파일시스템에서 이미지를 찾아오는 메서드
    @GetMapping("/{image-name}")
    public ResponseEntity getImage(@PathVariable("image-name") String imageName) throws IOException {
        byte[] imageData = imageManager.downloadImageFromFileSystem(imageName);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.IMAGE_PNG)        // 미디어타입이 PNG라고는 되어 있지만 크게 영향을 주지 않는다.
                .body(imageData);
    }
}
