package Baeksa.money.domain.s3.controller;

import Baeksa.money.domain.s3.service.S3Service;
import Baeksa.money.domain.s3.dto.S3ResDto;
import Baeksa.money.global.excepction.code.BaseApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api/s3")
@RequiredArgsConstructor
@RestController
public class S3Controller {

    private final S3Service s3Service;

//    @PostMapping("upload")
//    public ResponseEntity<?> uploadFile(MultipartFile multipartFile){
//        String savedFile = s3Service.saveFile(multipartFile);
//        return ResponseEntity.ok(new BaseApiResponse<>(200, "UPLOAD-FILE", "단일 파일 업로드", savedFile));
//    }

    @GetMapping("/upload-url")
    public ResponseEntity<?> getPresignedUrl(@RequestParam("fileName") String fileName,
                                             @RequestParam("fileType") String fileType) {
        try {
            S3ResDto result = s3Service.generatePresignedUrl(fileName, fileType);
            return ResponseEntity.ok(new BaseApiResponse<>(200, "SUCCESS", "Presigned URL 생성", result));
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(new BaseApiResponse<>(500, "ERROR", "URL 생성 실패", null));
        }
    }
}
