package Baeksa.money.domain.s3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class S3ResDto {
    private String presignedUrl;  // 업로드용 URL
    private String fileUrl;       // 최종 파일 URL
    private String fileName;      // 랜덤 파일명
}
