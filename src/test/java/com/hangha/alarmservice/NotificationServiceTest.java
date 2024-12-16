package com.hangha.alarmservice;

import com.hangha.alarmservice.Repository.ProductNotificationHistoryRepository;
import com.hangha.alarmservice.Repository.ProductRepository;
import com.hangha.alarmservice.Service.NotificationService;
import com.hangha.alarmservice.domain.Product;
import com.hangha.alarmservice.domain.StockStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;


import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductNotificationHistoryRepository historyRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testSendNotifications_Product1_OutOfStock() {
        // Given: 초기 테스트 데이터 생성
        Product product1 = productRepository.save(new Product(1, StockStatus.IN_STOCK));
        Product product2 = productRepository.save(new Product(2, StockStatus.IN_STOCK));

        // When: 1번 제품에 알림 발송 시도
        try {
            notificationService.sendNotifications(product1);
        } catch (IllegalStateException e) {
            System.out.println("1번 제품 재고 소진: " + e.getMessage());
        }

        // 영속성 컨텍스트 동기화
        entityManager.flush();
        entityManager.clear();

        // Then: 최신 상태 재조회
        Product updatedProduct1 = productRepository.findById(product1.getId()).orElseThrow();
        assertEquals(StockStatus.OUT_OF_STOCK, updatedProduct1.getStockStatus());
        System.out.println("1번 제품 상태: " + updatedProduct1.getStockStatus());

        // When: 2번 제품 알림 발송 시도
        notificationService.sendNotifications(product2);

        // Then: 2번 제품 상태 확인
        Product updatedProduct2 = productRepository.findById(product2.getId()).orElseThrow();
        assertEquals(StockStatus.IN_STOCK, updatedProduct2.getStockStatus());
        System.out.println("2번 제품 상태: " + updatedProduct2.getStockStatus());
    }
}
