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

    public void updateLastNotifiedUserId(Long lastNotifiedUserId) {
        this.lastNotifiedUserId = lastNotifiedUserId;
    }

    public void completeNotification() {
        this.notificationStatus = NotificationStatus.COMPLETED;
    }

    public void cancelDueToError() {
        this.notificationStatus = NotificationStatus.CANCELED_BY_ERROR;
    }


}
