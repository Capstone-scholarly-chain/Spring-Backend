package Baeksa.money.global.jwt;


import Baeksa.money.domain.Dto.MemberDto;
import Baeksa.money.global.redis.RefreshTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "JWT", description = "JWT 재발급 API")
public class ReissueController {

    private final RefreshTokenService refreshTokenService;

    public ReissueController(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        MemberDto.TokenResponse tokenResponse = refreshTokenService.refreshValid(request);
        return refreshTokenService.reissue(response, tokenResponse);
    }
}
