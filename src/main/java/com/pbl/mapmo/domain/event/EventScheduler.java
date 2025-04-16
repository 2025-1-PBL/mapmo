package com.pbl.mapmo.domain.event;

import com.pbl.mapmo.domain.common.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

    private final BrandService brandService;
    private final EventRepository eventRepository;

    /**
     * 매일 자정에 이벤트 상태를 업데이트하고 만료된 이벤트를 처리합니다.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void processEvents() {
        log.info("일일 이벤트 처리 작업 시작");

        try {
            // 이벤트 상태 업데이트 및 만료된 이벤트 처리
            int updatedCount = brandService.updateAllEventStatus();
            log.info("이벤트 상태 업데이트 완료: {} 개 업데이트됨", updatedCount);

            // 만료된 이벤트 삭제
            int deletedCount = brandService.deleteExpiredEvents();
            log.info("만료된 이벤트 삭제 완료: {} 개 삭제됨", deletedCount);

            // 날짜 없는 이벤트 처리
            int handledCount = brandService.handleUndefinedEvents();
            log.info("날짜 미지정 이벤트 처리 완료: {} 개 처리됨", handledCount);

            log.info("일일 이벤트 처리 작업 완료");
        } catch (Exception e) {
            log.error("이벤트 처리 중 오류 발생", e);
        }
    }
}