package Baeksa.money.domain.streams.service;


import Baeksa.money.domain.auth.Service.MemberService;
import Baeksa.money.domain.auth.Service.StudentValidService;
import Baeksa.money.domain.auth.converter.MemberConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamsService {

    private final StreamsProducer streamProducer;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void test(String message) {
        streamProducer.publishSignup2(message);
    }




}