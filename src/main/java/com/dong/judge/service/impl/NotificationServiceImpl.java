package com.dong.judge.service.impl;

import com.dong.judge.dao.repository.NotificationCombinedRepository;
import com.dong.judge.dao.repository.UserNotificationStatusRepository;
import com.dong.judge.model.pojo.notification.Notification;
import com.dong.judge.model.pojo.notification.UserNotificationStatus;
import com.dong.judge.model.pojo.user.User;
import com.dong.judge.service.NotificationService;
import com.dong.judge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统通知服务实现类
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationCombinedRepository notificationRepository;
    
    @Autowired
    private UserNotificationStatusRepository statusRepository;
    
    @Autowired
    private UserService userService;
    
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
        
        // 检查是否为全局通知
        if (GLOBAL_RECEIVER_ID.equals(notification.getReceiverId())) {
            // 获取所有用户列表
            List<User> allUsers = userService.list();
            
            // 为每个用户创建独立的通知
            for (User user : allUsers) {
                // 创建用户特定的通知副本
                Notification userNotification = new Notification();
                userNotification.setType(notification.getType());
                userNotification.setTitle(notification.getTitle());
                userNotification.setContent(notification.getContent());
                userNotification.setRead(false);
                userNotification.setReceiverId(user.getId().toString());
                userNotification.setCreatedAt(now);
                userNotification.setUpdatedAt(now);
                
                // 保存用户特定的通知
                notificationRepository.save(userNotification);
            }
            
            // 返回原始通知（仅用于API响应，实际上已为每个用户创建了独立通知）
            return notification;
        } else {
            // 非全局通知，直接保存
            return notificationRepository.save(notification);
        }
    }

    @Override
    public List<Notification> getUserNotifications(String userId) {
        // 如果用户邮箱为空，则返回空列表
        if (userId == null || userId.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取用户特定通知
        List<Notification> notifications = notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId);
        
        // 获取用户已删除的通知ID列表
        List<String> notificationIds = notifications.stream()
                .map(Notification::getId)
                .collect(Collectors.toList());
        
        // 如果没有通知，直接返回空列表
        if (notificationIds.isEmpty()) {
            return notifications;
        }
        
        // 获取用户的通知状态
        List<UserNotificationStatus> statuses = statusRepository
                .findByUserIdAndNotificationIdIn(userId, notificationIds);
        
        // 已删除的通知ID集合
        Set<String> deletedNotificationIds = statuses.stream()
                .filter(status -> status.getDeleted() != null && status.getDeleted())
                .map(UserNotificationStatus::getNotificationId)
                .collect(Collectors.toSet());
        
        // 过滤掉已删除的通知
        return notifications.stream()
                .filter(notification -> !deletedNotificationIds.contains(notification.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(String userId) {
        if (userId == null || userId.isEmpty()) {
            return 0;
        }
        
        // 获取用户的所有通知
        List<Notification> notifications = getUserNotifications(userId);
        
        // 如果没有通知，直接返回0
        if (notifications.isEmpty()) {
            return 0;
        }
        
        // 获取通知ID列表
        List<String> notificationIds = notifications.stream()
                .map(Notification::getId)
                .collect(Collectors.toList());
        
        // 获取用户已读的通知状态
        List<UserNotificationStatus> statuses = statusRepository
                .findByUserIdAndNotificationIdIn(userId, notificationIds);
        
        // 已读通知ID集合
        List<String> readNotificationIds = statuses.stream()
                .filter(UserNotificationStatus::getRead)
                .map(UserNotificationStatus::getNotificationId)
                .collect(Collectors.toList());
        
        // 未读数量 = 总通知数 - 已读通知数
        return notifications.size() - readNotificationIds.size();
    }

    @Override
    public Map<String, Object> getUserNotificationsWithStatus(String userId) {
        // 获取用户的所有通知
        List<Notification> notifications = getUserNotifications(userId);
        
        // 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("notifications", notifications);
        
        // 如果用户ID为空，则只返回通知列表
        if (userId.isEmpty()) {
            return result;
        }
        
        // 获取通知ID列表
        List<String> notificationIds = notifications.stream()
                .map(Notification::getId)
                .collect(Collectors.toList());
        
        // 获取用户的通知状态
        List<UserNotificationStatus> statuses = statusRepository
                .findByUserIdAndNotificationIdIn(userId, notificationIds);
        
        // 构建通知ID到状态的映射
        Map<String, Boolean> readStatusMap = new HashMap<>();
        
        // 已有状态的通知ID集合
        Set<String> existingNotificationIds = statuses.stream()
                .map(UserNotificationStatus::getNotificationId)
                .collect(Collectors.toSet());
        
        // 处理已有状态
        for (UserNotificationStatus status : statuses) {
            readStatusMap.put(status.getNotificationId(), status.getRead());
        }
        
        // 为没有状态的通知设置默认未读状态
        for (Notification notification : notifications) {
            String notificationId = notification.getId();
            if (!existingNotificationIds.contains(notificationId)) {
                readStatusMap.put(notificationId, false);
            }
        }
        
        // 添加已读状态到结果
        result.put("readStatusMap", readStatusMap);
        
        // 计算未读数量
        long unreadCount = notifications.size() - statuses.stream()
                .filter(status -> status.getRead() != null && status.getRead())
                .count();
        result.put("unreadCount", unreadCount);
        
        return result;
    }
    
    @Override
    public UserNotificationStatus getUserNotificationStatus(String userId, String notificationId) {
        if (userId == null || notificationId == null) {
            return null;
        }
        
        // 查找用户通知状态
        Optional<UserNotificationStatus> statusOpt = statusRepository
                .findByUserIdAndNotificationId(userId, notificationId);
        
        return statusOpt.orElse(null);
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
        
        // 更新通知本身的已读状态
        notification.setRead(true);
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        
        // 查找用户通知状态
        Optional<UserNotificationStatus> statusOpt = statusRepository
                .findByUserIdAndNotificationId(userId, notificationId);
        
        UserNotificationStatus status;
        if (statusOpt.isPresent()) {
            // 更新已有状态
            status = statusOpt.get();
            status.setRead(true);
            status.setUpdatedAt(LocalDateTime.now());
        } else {
            // 创建新状态
            status = UserNotificationStatus.builder()
                    .userId(userId)
                    .notificationId(notificationId)
                    .read(true)
                    .deleted(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }
        
        // 保存状态
        statusRepository.save(status);
        
        return true;
    }

    @Override
    public boolean markAllAsRead(String userId) {
        if (userId == null) {
            return false;
        }
        
        // 获取用户的所有通知
        List<Notification> notifications = getUserNotifications(userId);
        
        // 获取通知ID列表
        List<String> notificationIds = notifications.stream()
                .map(Notification::getId)
                .collect(Collectors.toList());
                
        // 获取用户的通知状态
        List<UserNotificationStatus> statuses = statusRepository
                .findByUserIdAndNotificationIdIn(userId, notificationIds);
                
        // 创建或更新所有通知状态为已读
        List<UserNotificationStatus> updatedStatuses = new ArrayList<>();
        
        // 已有状态的通知ID集合
        Set<String> existingNotificationIds = statuses.stream()
                .map(UserNotificationStatus::getNotificationId)
                .collect(Collectors.toSet());
                
        // 更新已有状态
        for (UserNotificationStatus status : statuses) {
            status.setRead(true);
            status.setUpdatedAt(LocalDateTime.now());
            updatedStatuses.add(status);
        }
        
        // 为没有状态的通知创建新状态
        for (Notification notification : notifications) {
            String notificationId = notification.getId();
            if (!existingNotificationIds.contains(notificationId)) {
                UserNotificationStatus newStatus = UserNotificationStatus.builder()
                        .userId(userId)
                        .notificationId(notificationId)
                        .read(true)
                        .deleted(false)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                updatedStatuses.add(newStatus);
            }
        }
        
        // 批量保存状态
        statusRepository.saveAll(updatedStatuses);
        
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
    
    @Override
    public boolean deleteUserNotification(String userId, String notificationId) {
        if (userId == null || notificationId == null) {
            return false;
        }
        
        // 检查通知是否存在
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            return false;
        }
        
        // 检查通知是否属于该用户
        if (!userId.equals(notification.getReceiverId()) && !GLOBAL_RECEIVER_ID.equals(notification.getReceiverId())) {
            return false;
        }
        
        // 如果是用户特定通知，直接删除通知本身
        if (userId.equals(notification.getReceiverId())) {
            notificationRepository.deleteById(notificationId);
            
            // 同时删除相关的用户通知状态
            Optional<UserNotificationStatus> statusOpt = statusRepository
                    .findByUserIdAndNotificationId(userId, notificationId);
            statusOpt.ifPresent(userNotificationStatus -> statusRepository.delete(userNotificationStatus));
        } else {
            // 如果是全局通知，只删除用户通知状态
            Optional<UserNotificationStatus> statusOpt = statusRepository
                    .findByUserIdAndNotificationId(userId, notificationId);
            
            // 如果状态不存在，创建一个标记为已删除的状态
            if (statusOpt.isEmpty()) {
                UserNotificationStatus status = UserNotificationStatus.builder()
                        .userId(userId)
                        .notificationId(notificationId)
                        .read(false) // 不改变已读状态
                        .deleted(true) // 标记为已删除
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                statusRepository.save(status);
            } else {
                // 更新用户通知状态为已删除
                UserNotificationStatus status = statusOpt.get();
                status.setDeleted(true);
                status.setUpdatedAt(LocalDateTime.now());
                statusRepository.save(status);
            }
        }
        
        return true;
    }
}