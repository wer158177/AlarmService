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


    @Transactional
    public void handleRestock(Long productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        product.restock();
        notificationService.sendNotifications(product);

        productRepository.save(product);
    }


    @Transactional
    public void handleOutOfStock(Long productId) {
        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 품절 처리
        product.markAsOutOfStock();
        productRepository.save(product);
    }
}
