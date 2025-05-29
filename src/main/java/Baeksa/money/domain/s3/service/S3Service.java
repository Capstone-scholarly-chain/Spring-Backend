package Baeksa.money.domain.s3.service;

import Baeksa.money.domain.s3.dto.S3ResDto;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {

    private final AmazonS3 amazonS3;
    private Set<String> uploadedFileNames = new HashSet<>();
    private Set<Long> uploadedFileSizes = new HashSet<>();

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

//    // 단일 파일 저장
//    public String saveFile(MultipartFile file) {
//        String randomFilename = generateRandomFilename(file);
//        log.info(" [ upload file ]: {}", randomFilename);
//
//        ObjectMetadata metadata = new ObjectMetadata();   //이게뭐야
//        metadata.setContentLength(file.getSize());
//        metadata.setContentType(file.getContentType());
//
//        try {
//            amazonS3.putObject(bucket, randomFilename, file.getInputStream(), metadata);
//        } catch (IOException e) {
//            log.error("IO error while uploading file: " + e.getMessage());
//            throw new RuntimeException(e);
//        } catch (AmazonS3Exception e){
//            log.error("Amazon S3 error while uploading file: " + e.getMessage());
//            //커스텀 예외
//        }
//        log.info("[ File uploaded ]: {}", randomFilename);
//
//        return amazonS3.getUrl(bucket, randomFilename).toString();
//    }

    //pre-signed URL
    public S3ResDto generatePresignedUrl(String originalFileName, String fileType) {
        validateFileExtension(originalFileName, fileType);
        String randomFilename = generateRandomFilename(originalFileName);

        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60; // 1시간 후 만료
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, randomFilename)
                .withMethod(HttpMethod.PUT) //aws의 http
                .withExpiration(expiration);
//                .withContentType(fileType);   //자동으로 해준대

        URL presignedUrl = amazonS3.generatePresignedUrl(request);
        String fileUrl = String.format("https://%s.s3.amazonaws.com/%s", bucket, randomFilename);

        return new S3ResDto(presignedUrl.toString(), fileUrl, randomFilename);
    }

    // 랜덤 파일명
    private String generateRandomFilename(String originalFileName){
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);
        }
        // randomFilename
        return UUID.randomUUID().toString() + extension;
    }

    // 파일 확장자 체크
    private String validateFileExtension(String originalFilename, String fileType) {
        log.info("[ 파일명 확인 ]: {}", originalFilename);

//        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        log.info("[ 추출된 확장자 ]: {}", fileType);

        List<String> allowedExtensions = Arrays.asList("jpg", "png", "gif", "jpeg");

        if (!allowedExtensions.contains(fileType)) {
            log.warn(" [ 잘못된 파일 확장자 형식 ]");
            //커스텀 예외
        }

        return fileType;
    }

}
