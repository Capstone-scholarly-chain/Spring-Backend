package Baeksa.money.global.redis;

import Baeksa.money.domain.auth.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JwtPayloadCacheDto {
    private String studentId;
    private String username;
    private String role;
    private String status;
}
