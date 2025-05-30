//package Baeksa.money.global.jwt;
//
//import Baeksa.money.domain.auth.Dto.MemberDto;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.util.concurrent.TimeUnit;
//
//@Service
//@RequiredArgsConstructor
//public class UserCacheService {
//
//    private final RedisTemplate<String, Object> redisTemplate;
//    private final CustomUserDetailsService userDetailsService;
//    private static final String PREFIX = "jwtPayload:";
//
//
//
//    public UserDetails getUserDetails(String studentId) {
//        String key = PREFIX + studentId;
//
//        // 캐시 조회
//        Object cached = redisTemplate.opsForValue().get(key);
//        if (cached != null && cached instanceof CustomUserDetails) {
//            return (CustomUserDetails) cached;
//        }
//
//        // 2. 없으면 DB 조회 후
//        UserDetails userDetails = userDetailsService.loadUserByUsername(studentId);
//        // 3. 캐시 저장하고 리턴
//        redisTemplate.opsForValue().set("userDetails:" + studentId, userDetails, 3600, TimeUnit.SECONDS);
//
//        return userDetails;
//    }
//
//    public void cachePayload(MemberDto.LoginResponse loginResponse) {
//        String key = PREFIX + loginResponse.getStudentId();
//        redisTemplate.opsForValue().set(key, loginResponse, Duration.ofHours(1));
//    }
//
//
//    public void evict(String studentId) {
//        redisTemplate.delete(PREFIX + studentId);
//    }
//
//}
