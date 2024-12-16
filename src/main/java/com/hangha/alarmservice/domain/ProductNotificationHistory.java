package com.hangha.alarmservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ProductNotificationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    private Integer restockRound;

    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;

    private Long lastNotifiedUserId;


    public ProductNotificationHistory(Product product, Integer restockRound, NotificationStatus notificationStatus, Long lastNotifiedUserId) {
        this.product = product;
        this.restockRound = restockRound;
        this.notificationStatus = notificationStatus;
        this.lastNotifiedUserId = lastNotifiedUserId;
    }




    // 상태 업데이트 메서드
    public void updateNotificationStatus(NotificationStatus status, Long userId) {
        this.notificationStatus = status;
        this.lastNotifiedUserId = userId;
    }

    // 알림 오류 처리 메서드
    public void markAsError(Long userId) {
        updateNotificationStatus(NotificationStatus.CANCELED_BY_ERROR, userId);
    }

    // 알림 완료 처리 메서드
    public void markAsCompleted() {
        updateNotificationStatus(NotificationStatus.COMPLETED, null);
    }
    public void handleStockSoldOut(Long userId){
        updateNotificationStatus(NotificationStatus.CANCELED_BY_SOLD_OUT,userId);
        throw new IllegalStateException("알림 발송 중 재고가 소진되었습니다.");
    }

    // 알림 이력 생성메서드
    public static ProductNotificationHistory create(Product product) {
        return new ProductNotificationHistory(
                product, product.getRestockRound(), NotificationStatus.IN_PROGRESS, null
        );
    }



}
