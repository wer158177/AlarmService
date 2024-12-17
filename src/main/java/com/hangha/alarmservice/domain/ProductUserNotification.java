package com.hangha.alarmservice.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class ProductUserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;


    private Long userId;


    private boolean active;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    public ProductUserNotification(Product product, Long userId) {
        this.product = product;
        this.userId = userId;
        this.active = true;
        this.createdDate = LocalDateTime.now();
        this.modifiedDate = LocalDateTime.now();
    }



}
