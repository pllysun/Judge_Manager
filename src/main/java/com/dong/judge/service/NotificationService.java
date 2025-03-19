package com.dong.judge.service;

import com.dong.judge.model.pojo.notification.Notification;

import java.util.List;

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
     * @param userId 用户ID，如果为null则获取全局通知
     * @return 通知列表
     */
    List<Notification> getUserNotifications(String userId);
    
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
     * 删除通知
     *
     * @param notificationId 通知ID
     * @return 是否成功
     */
    boolean deleteNotification(String notificationId);
}