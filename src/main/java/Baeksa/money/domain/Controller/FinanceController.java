package Baeksa.money.domain.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/finance")
public class FinanceController {



    @GetMapping("/main/{year}/{semester}")
    public String main(@PathVariable Integer year, @PathVariable Integer semester) {
        /// 블록체인 으로 year, semester보냄
        /// 받은 값을 캐싱.. 하고 보여줌
        /// 이벤트를 보고 캐시 업데이트
        System.out.println("연도: " + year);
        System.out.println("학기: " + semester);
        return "학기별 장부 메인";
    }

    @GetMapping("/main/{year}/{semester}/{eventId}")
    public String eventDetail(@PathVariable Integer year, @PathVariable Integer semester, @PathVariable Integer eventId) {
        System.out.println("연도: " + year);
        System.out.println("학기: " + semester);
        System.out.println("행사: " + eventId);
        return "행사 상세";
    }
}
