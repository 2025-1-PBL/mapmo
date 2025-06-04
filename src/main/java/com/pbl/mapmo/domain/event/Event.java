package com.pbl.mapmo.domain.event;

import com.pbl.mapmo.domain.brand.Brand;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 255)
    private String content;

    @Column(length = 255, unique = true)
    private String url;

    // 추가된 필드
    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    // 이벤트 상태 필드 추가
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EventStatus status;

    // 브랜드와 연결 (ManyToOne 관계)
    @ManyToOne
    @JoinColumn(name = "brand_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "event_ibfk_1"))
    private Brand brand;

    // 이벤트 상태를 나타내는 열거형
    public enum EventStatus {
        ACTIVE,      // 활성 이벤트
        EXPIRED,     // 만료된 이벤트
        UNDEFINED    // 기간이 정의되지 않은 이벤트
    }

    // 이벤트가 현재 활성 상태인지 확인하는 메서드
    public boolean isActive() {
        LocalDate today = LocalDate.now();

        // 시작일과 종료일이 모두 있는 경우
        if (startDate != null && endDate != null) {
            return !today.isBefore(startDate) && !today.isAfter(endDate);
        }
        // 시작일만 있는 경우
        else if (startDate != null) {
            return !today.isBefore(startDate);
        }
        // 종료일만 있는 경우
        else if (endDate != null) {
            return !today.isAfter(endDate);
        }
        // 둘 다 없는 경우
        else {
            return true; // 날짜가 없으면 기본적으로 활성 상태로 간주
        }
    }

    // 이벤트 상태 업데이트 메서드
    public void updateStatus() {
        if (startDate == null && endDate == null) {
            this.status = EventStatus.UNDEFINED;
        } else if (isActive()) {
            this.status = EventStatus.ACTIVE;
        } else {
            this.status = EventStatus.EXPIRED;
        }
    }
}
