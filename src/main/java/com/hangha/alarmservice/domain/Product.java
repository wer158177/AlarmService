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

    public Product(long l, int i, StockStatus stockStatus) {
        this.id = l;
        this.restockRound = i;
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



}



