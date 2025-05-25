package Baeksa.money.domain.fcm;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmFirestoreService {

    private final Firestore db;

    public String getTokenByStudentId(String studentId) throws Exception {

        log.info("FCM 토큰 조회 시도: studentId={}", studentId);
        CollectionReference fcmTokenCol = db.collection("fcmToken");

        // 역할 문서들을 모두 순회하면서 해당 학번을 가진 토큰 찾기
        for (QueryDocumentSnapshot doc : fcmTokenCol.get().get().getDocuments()) {

            if (doc.contains(studentId)) {
                String token = doc.getString(studentId);
                log.info("토큰 발견: studentId={}, token={}", studentId, token);
                return token;
            }
        }

        log.warn("토큰을 찾을 수 없음: studentId={}", studentId);
        return null;
    }

    public List<String> getAllTokensByCommittee() throws Exception {
        return getAllTokensByRole("ROLE_COMMITTEE");
    }

    public List<String> getAllTokensByStudent() throws Exception {
        return getAllTokensByRole("ROLE_STUDENT");
    }

    public List<String> getAllTokensByAdmin() throws Exception {
        return getAllTokensByRole("ROLE_ADMIN");
    }


    private List<String> getAllTokensByRole(String role) throws Exception {

        DocumentSnapshot roleDoc = db.collection("fcmToken").document(role).get().get();

        List<String> tokens = new ArrayList<>();

        if (roleDoc.exists()) {
            Map<String, Object> fields = roleDoc.getData();

            if (fields != null) {
                for (Object value : fields.values()) {
                    if (value instanceof String token) {
                        tokens.add(token);
                    }
                }
            }
        }

        return tokens;
    }


    public List<String> getAllTokensByAll() throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference fcmTokenCol = db.collection("fcmToken");

        // 우리가 찾고 싶은 역할 목록
        List<String> targetRoles = List.of("ROLE_COMMITTEE", "ROLE_STUDENT");

        List<String> tokens = new ArrayList<>();

        for (String role : targetRoles) {
            DocumentSnapshot roleDoc = fcmTokenCol.document(role).get().get();

            if (roleDoc.exists()) {
                Map<String, Object> fields = roleDoc.getData();  // 학번 → token

                if (fields != null) {
                    for (Object value : fields.values()) {
                        if (value instanceof String token) {
                            tokens.add(token);
                        }
                    }
                }
            }
        }

        return tokens;
    }


    // 테스트용 메서드
    public List<String> getTokensFromListField(String role) throws Exception {
        DocumentSnapshot roleDoc = db.collection("fcmToken").document(role).get().get();

        List<String> tokens = new ArrayList<>();

        if (roleDoc.exists()) {
            Object listField = roleDoc.get("list");
            log.info("list 필드 타입: {}", listField != null ? listField.getClass().getSimpleName() : "null");
            log.info("list 필드 내용: {}", listField);

            if (listField instanceof List<?> tokenList) {
                for (int i = 0; i < tokenList.size(); i++) {
                    Object token = tokenList.get(i);
                    log.info("list[{}]: {}", i, token);
                    if (token instanceof String) {
                        tokens.add((String) token);
                    }
                }
            }
        }

        return tokens;
    }
}


