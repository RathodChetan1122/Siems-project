package com.siems.service.impl;

import com.siems.dto.common.PageResponse;
import com.siems.dto.notification.NotificationResponse;
import com.siems.entity.Notification;
import com.siems.repository.NotificationRepository;
import com.siems.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public PageResponse<NotificationResponse> getForUser(Long userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUser_UserIdAndReadFalse(userId);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
