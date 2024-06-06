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

    // 이미지를 파일 시스템에 업로드하는 메서드
    public String uploadImageToFileSystem(MultipartFile file) throws IOException {
        String fileExtension = getFileExtension(file.getOriginalFilename());    // 파일의 확장자명을 추출한다.
        String UUIDName = UUID.randomUUID().toString();     // 파일에 랜덤 UUID를 부여한다.
        String filePath = FILE_PATH+ UUIDName +fileExtension;   // 파일 경로를 설정한다.

        // 이미지파일 관련 정보를 데이터베이스에 저장한다.
        FileData fileData = fileDataRepository.save(FileData.builder()
                .name(UUIDName+fileExtension)   // 파일 이름
                .type(file.getContentType())    // 이미지 타입
                .filePath(filePath)     // 파일 경로
                .build());  // 를 저장한다.

        file.transferTo(new File(filePath));    // 이미지를 리눅스 내 지정된 파일 시스템에 저장한다.

        if(fileData != null){
            return UUIDName+fileExtension;  // 저장된 파일의 파일명을 리턴한다.
        }
        return null;
    }

    // 이미지를 파일시스템에서 다운로드하는 메서드
    public byte[] downloadImageFromFileSystem(String fileName) throws IOException {
        Optional<FileData> optionalFileData = fileDataRepository.findByName(fileName);  // 데이터베이스에서 파일을 탐색한다.
        String filePath = optionalFileData.get().getFilePath();     // 찾은 파일데이터에서 파일경로를 불러온다.
        byte[] images = Files.readAllBytes(new File(filePath).toPath());    // 이미지를 파일시스템에서 불러오기한다.
        return images;  // 이미지를 리턴한다.
    }

    // 이미지를 파일시스템에서 삭제하는 메서드
    public void deleteImage(String fileName){
        Optional<FileData> optionalFileData = fileDataRepository.findByName(fileName);  // 데이터베이스에서 파일을 탐색한다.
        String filePath = optionalFileData.get().getFilePath();     // 찾은 파일데이터에서 파일 경로를 불러온다.
        File image = new File(filePath);        // 이미지를 지정한다.
        image.delete();     // 이미지를 파일시스템에서 삭제한다.
        fileDataRepository.delete(optionalFileData.get());     // 데이터베이스에서 해당 이미지 관련 데이터를 삭제한다.
    }

    // 이미지파일의 학장자명을 추출하는 메서드
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
