package com.hangha.alarmservice.Service;

import com.hangha.alarmservice.Repository.ProductNotificationHistoryRepository;
import com.hangha.alarmservice.Repository.ProductUserNotificationRepository;
import com.hangha.alarmservice.Repository.ProductUserNotificationHistoryRepository;
import com.hangha.alarmservice.domain.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final ProductNotificationHistoryRepository historyRepository;
    private final ProductUserNotificationRepository userNotificationRepository;
    private final ProductUserNotificationHistoryRepository userNotificationHistoryRepository;

    public NotificationService(ProductUserNotificationRepository userNotificationRepository,
                               ProductNotificationHistoryRepository historyRepository,
                               ProductUserNotificationHistoryRepository userNotificationHistoryRepository) {
        this.historyRepository = historyRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.userNotificationHistoryRepository = userNotificationHistoryRepository;
    }

    @Transactional
    public void sendNotifications(Product product) {
        // 알림 신청한 유저 목록 조회
        List<ProductUserNotification> notifications = userNotificationRepository.findByProductAndActiveTrue(product);

        // 재입고 발송 히스토리 작성
        ProductNotificationHistory currentHistory = new ProductNotificationHistory(
                product,
                product.getRestockRound(),
                NotificationStatus.IN_PROGRESS,
                null
        );
        historyRepository.save(currentHistory);

        // 유저별 알림 발송 로직
        for (ProductUserNotification notification : notifications) {
            try {
                // 유저 알림 발송 기록 저장
                ProductUserNotificationHistory userHistory = new ProductUserNotificationHistory(
                        product,
                        notification.getUserId(),
                        product.getRestockRound(),
                        LocalDateTime.now()
                );

                userNotificationHistoryRepository.save(userHistory);

                // 마지막 발송된 유저 ID 업데이트
                currentHistory.updateLastNotifiedUserId(notification.getUserId());
                historyRepository.save(currentHistory);

                System.out.printf("유저 ID %d에게 상품 ID %d 알림 발송 기록 저장 완료%n",
                        notification.getUserId(), product.getId());

            } catch (Exception e) {
                currentHistory.cancelDueToError();
                currentHistory.updateLastNotifiedUserId(notification.getUserId());
                historyRepository.save(currentHistory);
                throw new RuntimeException("알림 발송 오류 발생", e);
            }
        }

        // 발송 완료 처리
        currentHistory.completeNotification();
        historyRepository.save(currentHistory);
    }

}