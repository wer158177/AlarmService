package com.hangha.alarmservice.config;



import com.hangha.alarmservice.Repository.ProductRepository;
import com.hangha.alarmservice.Repository.ProductUserNotificationRepository;
import com.hangha.alarmservice.domain.Product;
import com.hangha.alarmservice.domain.ProductUserNotification;
import com.hangha.alarmservice.domain.StockStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.LongStream;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository,
                                   ProductUserNotificationRepository userNotificationRepository) {
        return args -> {
            // 1번 상품 생성
            Product product = new Product( 0, StockStatus.OUT_OF_STOCK);
            Product product2 = new Product( 0, StockStatus.OUT_OF_STOCK);
            productRepository.save(product);
            productRepository.save(product2);

            // 500명 유저 알림 신청 생성
            LongStream.rangeClosed(0, 30).forEach(userId -> {
                ProductUserNotification notification = new ProductUserNotification(product, userId);
                userNotificationRepository.save(notification);
            });

            LongStream.rangeClosed(0,30).forEach(userId -> {
                ProductUserNotification notification = new ProductUserNotification(product2, userId);
                userNotificationRepository.save(notification);
            });

            System.out.println("1번 상품과 500명 알림 신청자 생성 완료");
        };
    }
}
