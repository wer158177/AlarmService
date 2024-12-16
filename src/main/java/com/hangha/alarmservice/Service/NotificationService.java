package com.hangha.alarmservice.Service;

import com.hangha.alarmservice.Repository.ProductNotificationHistoryRepository;
import com.hangha.alarmservice.Repository.ProductUserNotificationRepository;
import com.hangha.alarmservice.Repository.ProductUserNotificationHistoryRepository;
import com.hangha.alarmservice.domain.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;

@Service
public class NotificationService {

    private final ProductNotificationHistoryRepository historyRepository;
    private final ProductUserNotificationRepository userNotificationRepository;
    private final ProductUserNotificationHistoryRepository userNotificationHistoryRepository;

    // 생성자 주입
    public NotificationService(ProductUserNotificationRepository userNotificationRepository,
                               ProductNotificationHistoryRepository historyRepository,
                               ProductUserNotificationHistoryRepository userNotificationHistoryRepository) {
        this.historyRepository = historyRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.userNotificationHistoryRepository = userNotificationHistoryRepository;
    }

    // 알림 발송 메인 메서드
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotifications(Product product) {
        List<ProductUserNotification> notifications = userNotificationRepository.findByProductAndActiveTrue(product);
        ProductNotificationHistory currentHistory = initializeNotificationHistory(product);

        try {
            for (ProductUserNotification notification : notifications) {
                processNotification(product, currentHistory, notification);  // 유저별 발송 시도
            }
            completeNotification(currentHistory);  // 모든 발송 완료
        } catch (IllegalStateException e) {
            handleStockSoldOut(product, currentHistory);  // 재고 소진 처리
        }
    }

    // 알림 발송 이력 초기화 메서드
    private ProductNotificationHistory initializeNotificationHistory(Product product) {
        ProductNotificationHistory history = ProductNotificationHistory.create(product);
        historyRepository.save(history);
        return history;
    }

    // 개별 유저 알림 발송 처리 메서드
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processNotification(Product product, ProductNotificationHistory currentHistory, ProductUserNotification notification) {
        try {
            // 재고 상태 확인
            checkProductStock(product,notification);
            // 유저 알림 발송
            sendUserNotification(product, notification);
            // 발송 상태 업데이트
            updateNotificationProgress(currentHistory, notification);
        } catch (IllegalStateException e) {
            currentHistory.updateNotificationStatus(NotificationStatus.CANCELED_BY_SOLD_OUT, notification.getUserId());
            historyRepository.save(currentHistory);
            throw e;  // 발송 중단
        } catch (Exception e) {
            // 오류 발생 시 오류 처리
            handleNotificationError(currentHistory, notification, e);
        }
    }

    // 재고 상태 확인 메서드
    private void checkProductStock(Product product,ProductUserNotification notification) {
        if (product.isOutOfStock()) {
            throw new IllegalStateException("재고 소진 발생: 유저 ID " + notification.getUserId());
        }
    }

    // 유저에게 알림 발송 기록 저장 메서드
    private void sendUserNotification(Product product, ProductUserNotification notification) {
        ProductUserNotificationHistory userHistory = ProductUserNotificationHistory.create(product, notification.getUserId());
        userNotificationHistoryRepository.save(userHistory);
    }

    // 알림 발송 진행 상태 업데이트 메서드
    private void updateNotificationProgress(ProductNotificationHistory currentHistory, ProductUserNotification notification) {
        currentHistory.updateNotificationStatus(NotificationStatus.IN_PROGRESS, notification.getUserId());
        historyRepository.save(currentHistory);
    }

    // 알림 발송 오류 처리 메서드
    private void handleNotificationError(ProductNotificationHistory currentHistory, ProductUserNotification notification, Exception e) {
        currentHistory.markAsError(notification.getUserId());
        historyRepository.save(currentHistory);
        throw new RuntimeException("알림 발송 오류 발생", e);
    }

    // 알림 발송 완료 처리 메서드
    private void completeNotification(ProductNotificationHistory currentHistory) {
        if (currentHistory.getLastNotifiedUserId() != null) {
            // 마지막 발송 유저 ID 업데이트 후 완료 처리
            currentHistory.updateNotificationStatus(NotificationStatus.COMPLETED, currentHistory.getLastNotifiedUserId());
        } else {

            currentHistory.markAsCompleted();
        }
        historyRepository.save(currentHistory);
    }

    // 재고 소진 처리 메서드
    private void handleStockSoldOut(Product product, ProductNotificationHistory currentHistory) {
        product.markAsOutOfStock();
        currentHistory.updateNotificationStatus(NotificationStatus.CANCELED_BY_SOLD_OUT, currentHistory.getLastNotifiedUserId());
        historyRepository.save(currentHistory);
    }
}
