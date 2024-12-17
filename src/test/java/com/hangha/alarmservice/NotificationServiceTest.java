package com.hangha.alarmservice;

import com.hangha.alarmservice.Repository.ProductNotificationHistoryRepository;
import com.hangha.alarmservice.Repository.ProductUserNotificationRepository;
import com.hangha.alarmservice.Repository.ProductUserNotificationHistoryRepository;
import com.hangha.alarmservice.Service.NotificationService;
import com.hangha.alarmservice.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private ProductNotificationHistoryRepository historyRepository;

    @Mock
    private ProductUserNotificationRepository userNotificationRepository;

    @Mock
    private ProductUserNotificationHistoryRepository userHistoryRepository;

    private Product testProduct;
    private ProductNotificationHistory testHistory;

    @BeforeEach
    void setUp() {
        testProduct = new Product(1, StockStatus.IN_STOCK);
        testHistory = new ProductNotificationHistory(testProduct, 1, NotificationStatus.IN_PROGRESS, null);
    }

    @Test
    void testSendNotifications_Success() {
        // Given
        ProductUserNotification user1 = new ProductUserNotification(testProduct, 101L);
        ProductUserNotification user2 = new ProductUserNotification(testProduct, 102L);

        when(historyRepository.save(any())).thenReturn(testHistory);
        when(userNotificationRepository.findByProductAndActiveTrue(any(Product.class)))
                .thenReturn(List.of(user1, user2));

        // When
        notificationService.sendNotifications(testProduct);

        // Then
        verify(userHistoryRepository, times(2)).save(any(ProductUserNotificationHistory.class));
        verify(historyRepository, atLeast(3)).save(any(ProductNotificationHistory.class));
    }

    @Test
    void testSendNotifications_NoUsers() {
        // Given
        when(historyRepository.save(any())).thenReturn(testHistory);
        when(userNotificationRepository.findByProductAndActiveTrue(any(Product.class)))
                .thenReturn(List.of());

        // When
        notificationService.sendNotifications(testProduct);

        // Then
        verify(userHistoryRepository, never()).save(any());
        verify(historyRepository, times(2)).save(any(ProductNotificationHistory.class));
    }

    @Test
    void testSendNotifications_StockSoldOut() {
        // Given
        testProduct.markAsOutOfStock(); // 상태를 품절로 변경
        assertTrue(testProduct.isOutOfStock(), "상품 상태가 OUT_OF_STOCK이어야 합니다.");
        ProductUserNotification user1 = new ProductUserNotification(testProduct, 101L);

        when(userNotificationRepository.findByProductAndActiveTrue(any(Product.class)))
                .thenReturn(List.of(user1));
        when(historyRepository.save(any())).thenReturn(testHistory);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> notificationService.sendNotifications(testProduct));

        assertEquals("재고 소진 발생: 유저 ID 101", exception.getMessage());
        verify(historyRepository, atLeastOnce()).save(any(ProductNotificationHistory.class));
    }

    @Test
    void testSendNotifications_ErrorDuringProcessing() {
        // Given
        ProductUserNotification user1 = new ProductUserNotification(testProduct, 101L);
        ProductUserNotification user2 = new ProductUserNotification(testProduct, 102L);

        when(historyRepository.save(any())).thenReturn(testHistory);
        when(userNotificationRepository.findByProductAndActiveTrue(any(Product.class)))
                .thenReturn(List.of(user1, user2));
        doThrow(new RuntimeException("발송 오류")).when(userHistoryRepository).save(any());

        // When & Then
        assertThrows(RuntimeException.class, () -> notificationService.sendNotifications(testProduct));
        verify(historyRepository, atLeastOnce()).save(any());
    }
}
