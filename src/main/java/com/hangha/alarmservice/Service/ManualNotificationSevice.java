package com.hangha.alarmservice.Service;

import com.hangha.alarmservice.Repository.ProductNotificationHistoryRepository;
import com.hangha.alarmservice.Repository.ProductUserNotificationHistoryRepository;
import com.hangha.alarmservice.Repository.ProductUserNotificationRepository;
import com.hangha.alarmservice.domain.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ManualNotificationSevice {

    private final ProductNotificationHistoryRepository productNotificationHistoryRepository;
    private final ProductUserNotificationRepository userNotificationRepository;
    private final ProductUserNotificationHistoryRepository userNotificationHistoryRepository;

    public ManualNotificationSevice(ProductNotificationHistoryRepository productNotificationHistoryRepository, ProductUserNotificationRepository userNotificationRepository, ProductUserNotificationHistoryRepository userNotificationHistoryRepository) {
        this.productNotificationHistoryRepository = productNotificationHistoryRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.userNotificationHistoryRepository = userNotificationHistoryRepository;
    }


    @Transactional
    public void resendNotifications(Long productId) {
        ProductNotificationHistory lastHistory = fetchLastNotificationHistory(productId);
        List<ProductUserNotification> remainingNotifications = fetchRemainingNotifications(productId, lastHistory.getLastNotifiedUserId());
        processNotifications(remainingNotifications, lastHistory);
        completeNotification(lastHistory);
    }

    // 1. 알림 히스토리에서 마지막 발송 실패한 유저 조회
    private ProductNotificationHistory fetchLastNotificationHistory(Long productId) {
        return productNotificationHistoryRepository
                .findTopByProductIdOrderByLastNotifiedUserIdDesc(productId)
                .orElseThrow(() -> new IllegalStateException("이전 발송 실패 기록이 없습니다."));
    }

    // 2. 마지막 발송 실패한 유저부터 신청자 목록 조회
    private List<ProductUserNotification> fetchRemainingNotifications(Long productId, Long lastUserId) {
        List<ProductUserNotification> remainingNotifications = userNotificationRepository
                .findByProductIdAndUserIdGreaterThanEqualOrderByUserIdAsc(productId, lastUserId);

        if (remainingNotifications.isEmpty()) {
            throw new IllegalStateException("남은 발송 대상자가 없습니다.");
        }
        return remainingNotifications;
    }

    // 3. 알림 발송 처리
    private void processNotifications(List<ProductUserNotification> notifications, ProductNotificationHistory lastHistory) {
        for (ProductUserNotification notification : notifications) {
            try {
                saveUserNotificationHistory(lastHistory.getProduct(), notification);
                updateNotificationProgress(lastHistory, notification);
            } catch (Exception e) {
                handleNotificationError(lastHistory, notification);
                throw new RuntimeException("알림 발송 실패: 유저 ID " + notification.getUserId(), e);
            }
        }
    }

    // 개별 유저 발송 기록 저장
    private void saveUserNotificationHistory(Product product, ProductUserNotification notification) {
        ProductUserNotificationHistory userHistory = ProductUserNotificationHistory.create(product, notification.getUserId());
        userNotificationHistoryRepository.save(userHistory);
    }

    // 알림 진행 상태 업데이트
    private void updateNotificationProgress(ProductNotificationHistory lastHistory, ProductUserNotification notification) {
        lastHistory.updateNotificationStatus(NotificationStatus.IN_PROGRESS, notification.getUserId());
        productNotificationHistoryRepository.save(lastHistory);
    }

    // 알림 오류 처리
    private void handleNotificationError(ProductNotificationHistory lastHistory, ProductUserNotification notification) {
        lastHistory.updateNotificationStatus(NotificationStatus.CANCELED_BY_ERROR, notification.getUserId());
        productNotificationHistoryRepository.save(lastHistory);
    }

    // 4. 모든 알림 발송 완료 처리
    private void completeNotification(ProductNotificationHistory lastHistory) {
        lastHistory.updateNotificationStatus(NotificationStatus.COMPLETED, lastHistory.getLastNotifiedUserId());
        productNotificationHistoryRepository.save(lastHistory);
    }
}