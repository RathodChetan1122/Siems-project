package com.siems.service;

import com.siems.dto.common.PageResponse;
import com.siems.dto.notification.NotificationResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    PageResponse<NotificationResponse> getForUser(Long userId, Pageable pageable);
    long getUnreadCount(Long userId);
}
