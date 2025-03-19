package com.dong.judge.service.impl;

import com.dong.judge.dao.repository.NotificationRepository;
import com.dong.judge.model.pojo.notification.Notification;
import com.dong.judge.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 系统通知服务实现类
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    
    // 全局通知的接收者ID
    private static final String GLOBAL_RECEIVER_ID = "all";

    @Override
    public Notification createNotification(Notification notification) {
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        notification.setCreatedAt(now);
        notification.setUpdatedAt(now);
        
        // 默认未读
        notification.setRead(false);
        
        // 保存通知
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getUserNotifications(String userId) {
        // 如果用户ID为空，则只返回全局通知
        if (userId == null || userId.isEmpty()) {
            return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(GLOBAL_RECEIVER_ID);
        }
        
        // 返回全局通知和用户特定通知
        return notificationRepository.findByReceiverIdInOrderByCreatedAtDesc(
                Arrays.asList(GLOBAL_RECEIVER_ID, userId)
        );
    }

    @Override
    public long getUnreadCount(String userId) {
        if (userId == null || userId.isEmpty()) {
            return 0;
        }
        
        // 统计全局通知和用户特定通知中的未读数量
        return notificationRepository.countByReceiverIdInAndRead(
                Arrays.asList(GLOBAL_RECEIVER_ID, userId),
                false
        );
    }

    @Override
    public boolean markAsRead(String notificationId, String userId) {
        if (notificationId == null || userId == null) {
            return false;
        }
        
        // 查找通知
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            return false;
        }
        
        // 检查通知是否是全局通知或者是发给该用户的
        if (!GLOBAL_RECEIVER_ID.equals(notification.getReceiverId()) && 
                !userId.equals(notification.getReceiverId())) {
            return false;
        }
        
        // 标记为已读
        notification.setRead(true);
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        
        return true;
    }

    @Override
    public boolean markAllAsRead(String userId) {
        if (userId == null) {
            return false;
        }
        
        // 获取用户的所有通知
        List<Notification> notifications = getUserNotifications(userId);
        
        // 标记所有通知为已读
        for (Notification notification : notifications) {
            notification.setRead(true);
            notification.setUpdatedAt(LocalDateTime.now());
        }
        
        // 保存更新
        notificationRepository.saveAll(notifications);
        
        return true;
    }

    @Override
    public boolean deleteNotification(String notificationId) {
        if (notificationId == null) {
            return false;
        }
        
        // 检查通知是否存在
        if (!notificationRepository.existsById(notificationId)) {
            return false;
        }
        
        // 删除通知
        notificationRepository.deleteById(notificationId);
        
        return true;
    }
}