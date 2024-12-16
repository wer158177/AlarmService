package com.hangha.alarmservice.Service;

import com.hangha.alarmservice.Repository.ProductRepository;
import com.hangha.alarmservice.domain.Product;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public ProductService(ProductRepository productRepository, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }


    // 재입고 처리
    @Transactional
    public void handleRestock(Long productId) {
        Product product = findProductById(productId);

        product.restock();  // 재입고 상태 업데이트
        productRepository.save(product);  // 변경된 상태 저장

        try {
            notificationService.sendNotifications(product);
        } catch (Exception e) {
            System.out.println("알림 발송 오류: " + e.getMessage());
        }
    }

    // 품절 처리
    @Transactional
    public void handleOutOfStock(Long productId) {
        Product product = findProductById(productId);

        product.markAsOutOfStock();  // 품절 상태 업데이트
        productRepository.save(product);  // 변경된 상태 저장
    }



    // 공통 상품 조회 메서드
    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }
}
