package com.hangha.alarmservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class ProductUserNotificationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;
    private Long userId;
    private int restockRound;
    private LocalDateTime sentAt;

    public ProductUserNotificationHistory(Product product, Long userId, int restockRound, LocalDateTime sentAt) {
        this.product = product;
        this.userId = userId;
        this.restockRound = restockRound;
        this.sentAt = sentAt;
    }
}
