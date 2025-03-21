package com.dong.judge.service;

import com.dong.judge.model.pojo.notification.Notification;
import com.dong.judge.model.pojo.notification.UserNotificationStatus;

import java.util.List;
import java.util.Map;

/**
 * 系统通知服务接口
 * <p>
 * 提供系统通知相关的业务逻辑方法
 * </p>
 */
public interface NotificationService {
    
    /**
     * 创建系统通知
     *
     * @param notification 通知实体
     * @return 创建的通知
     */
    Notification createNotification(Notification notification);
    
    /**
     * 获取用户的通知列表
     *
     * @param email 用户邮箱，如果为null则获取全局通知
     * @return 通知列表
     */
    List<Notification> getUserNotifications(String email);
    
    /**
     * 获取所有未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    long getUnreadCount(String userId);
    
    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAsRead(String notificationId, String userId);
    
    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllAsRead(String userId);
    
    /**
     * 删除通知（全局删除）
     *
     * @param notificationId 通知ID
     * @return 是否成功
     */
    boolean deleteNotification(String notificationId);
    
    /**
     * 删除用户的通知状态（仅对特定用户删除通知）
     *
     * @param userId 用户ID
     * @param notificationId 通知ID
     * @return 是否成功
     */
    boolean deleteUserNotification(String userId, String notificationId);
    
    /**
     * 获取用户的通知列表及其状态
     *
     * @param email 用户邮箱，如果为null则获取全局通知
     * @return 包含通知列表、已读状态映射和未读数量的Map
     */
    Map<String, Object> getUserNotificationsWithStatus(String email);
    
    /**
     * 获取用户对特定通知的状态
     *
     * @param userId 用户ID
     * @param notificationId 通知ID
     * @return 用户通知状态
     */
    UserNotificationStatus getUserNotificationStatus(String userId, String notificationId);
}