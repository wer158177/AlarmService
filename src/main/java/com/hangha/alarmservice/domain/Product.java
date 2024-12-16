package com.hangha.alarmservice.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int restockRound;


    @Enumerated(EnumType.STRING)
    private StockStatus stockStatus;

    public Product( int restockRound, StockStatus stockStatus) {
        this.restockRound = restockRound;
        this.stockStatus = stockStatus;

    }


    //입고처리
    public void restock() {
        this.restockRound++;
        this.stockStatus = StockStatus.IN_STOCK;
    }
    //품절처리
    public void markAsOutOfStock() {
        this.stockStatus = StockStatus.OUT_OF_STOCK;
    }

    // 재고 상태 확인 메서드
    public boolean isInStock() {
        return this.stockStatus == StockStatus.IN_STOCK;
    }

    public boolean isOutOfStock() {
        return this.stockStatus == StockStatus.OUT_OF_STOCK;
    }



}



