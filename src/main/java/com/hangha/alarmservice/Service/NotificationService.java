package com.hangha.alarmservice.Service;

import com.hangha.alarmservice.Repository.ProductNotificationHistoryRepository;
import com.hangha.alarmservice.Repository.ProductUserNotificationRepository;
import com.hangha.alarmservice.Repository.ProductUserNotificationHistoryRepository;
import com.hangha.alarmservice.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final ProductNotificationHistoryRepository historyRepository;
    private final ProductUserNotificationRepository userNotificationRepository;
    private final ProductUserNotificationHistoryRepository userNotificationHistoryRepository;

    // 생성자 주입: 필요한 리포지토리들을 주입받아 초기화
    public NotificationService(ProductUserNotificationRepository userNotificationRepository,
                               ProductNotificationHistoryRepository historyRepository,
                               ProductUserNotificationHistoryRepository userNotificationHistoryRepository) {
        this.historyRepository = historyRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.userNotificationHistoryRepository = userNotificationHistoryRepository;
    }


    public void sendNotifications(Product product) {
        // 활성화된 알림 대상 사용자 조회
        List<ProductUserNotification> notifications = userNotificationRepository.findByProductAndActiveTrue(product);

        // 알림 이력 초기화
        ProductNotificationHistory currentHistory = initializeNotificationHistory(product);

        // 각 사용자에게 알림 발송 시도
        for (ProductUserNotification notification : notifications) {
            try {
                checkProductStock(product, notification); // 재고 상태 확인
                sendUserNotification(product, notification); // 알림 발송 기록 저장
                updateNotificationProgress(currentHistory, notification); // 진행 상태 업데이트
            } catch (IllegalStateException e) {
                // 재고 소진 시 처리
                handleStockSoldOut(product, currentHistory);
                throw e; // 예외 다시 던짐
            } catch (Exception e) {
                // 알림 발송 중 오류 발생 시 처리
                handleNotificationError(currentHistory, notification, e);
            }
        }

        // 알림 발송 완료 처리
        completeNotification(currentHistory);
    }


    private ProductNotificationHistory initializeNotificationHistory(Product product) {
        ProductNotificationHistory history = ProductNotificationHistory.create(product);
        return historyRepository.save(history);
    }


    private void checkProductStock(Product product, ProductUserNotification notification) {
        if (product.isOutOfStock()) {
            throw new IllegalStateException("재고 소진 발생: 유저 ID " + notification.getUserId());
        }
    }


    private void sendUserNotification(Product product, ProductUserNotification notification) {
        ProductUserNotificationHistory userHistory =
                ProductUserNotificationHistory.create(product, notification.getUserId());
        userNotificationHistoryRepository.save(userHistory);
    }


    private void updateNotificationProgress(ProductNotificationHistory currentHistory,
                                            ProductUserNotification notification) {
        currentHistory.updateNotificationStatus(NotificationStatus.IN_PROGRESS, notification.getUserId());
        historyRepository.save(currentHistory);
    }


    private void handleNotificationError(ProductNotificationHistory currentHistory,
                                         ProductUserNotification notification, Exception e) {
        currentHistory.markAsError(notification.getUserId());
        historyRepository.save(currentHistory);
        throw new RuntimeException("알림 발송 오류 발생", e);
    }


    private void completeNotification(ProductNotificationHistory currentHistory) {
        currentHistory.markAsCompleted();
        historyRepository.save(currentHistory);
    }


    private void handleStockSoldOut(Product product, ProductNotificationHistory currentHistory) {
        product.markAsOutOfStock(); // 상품 상태를 품절로 변경
        currentHistory.updateNotificationStatus(NotificationStatus.CANCELED_BY_SOLD_OUT, null);
        historyRepository.save(currentHistory);
    }
}
