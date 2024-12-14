package com.hangha.alarmservice.Repository;

import com.hangha.alarmservice.domain.ProductNotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductNotificationHistoryRepository extends JpaRepository<ProductNotificationHistory,Long> {
}
