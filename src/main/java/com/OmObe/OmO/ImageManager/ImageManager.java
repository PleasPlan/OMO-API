package com.OmObe.OmO.ImageManager;

import com.OmObe.OmO.Review.entity.FileData;
import com.OmObe.OmO.Review.repository.FileDataRepository;
import com.OmObe.OmO.auth.jwt.TokenDecryption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Component
public class ImageManager {

    @Value("${image.file-path}")
    private String FILE_PATH;

    private final FileDataRepository fileDataRepository;

    public ImageManager(FileDataRepository fileDataRepository) {
        this.fileDataRepository = fileDataRepository;
    }

    public String uploadImageToFileSystem(MultipartFile file) throws IOException {

        String fileExtension = getFileExtension(file.getOriginalFilename());
        String UUIDName = UUID.randomUUID().toString();
        String filePath = FILE_PATH+ UUIDName +fileExtension;

        FileData fileData = fileDataRepository.save(FileData.builder()
                .name(UUIDName+fileExtension)
                .type(file.getContentType())
                .filePath(filePath)
                .build());

        file.transferTo(new File(filePath));

        if(fileData != null){
            return UUIDName+fileExtension;
        }
        return null;
    }

    public byte[] downloadImageFromFileSystem(String fileName) throws IOException {
        Optional<FileData> optionalFileData = fileDataRepository.findByName(fileName);
        String filePath = optionalFileData.get().getFilePath();
        byte[] images = Files.readAllBytes(new File(filePath).toPath());
        return images;
    }

    public void deleteImage(String fileName){
        Optional<FileData> optionalFileData = fileDataRepository.findByName(fileName);
        String filePath = optionalFileData.get().getFilePath();
        File image = new File(filePath);
        image.delete();
        fileDataRepository.delete(optionalFileData.get());
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
