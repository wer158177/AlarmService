package com.hangha.alarmservice.Repository;

import com.hangha.alarmservice.domain.Product;
import com.hangha.alarmservice.domain.ProductUserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductUserNotificationRepository extends JpaRepository<ProductUserNotification, Long> {
    // 특정 상품의 알림 설정 사용자 조회
    List<ProductUserNotification> findByProductAndActiveTrue(Product product);

    List<ProductUserNotification> findByProductIdAndUserIdGreaterThanEqualOrderByUserIdAsc(Long productId, Long userId);



}
