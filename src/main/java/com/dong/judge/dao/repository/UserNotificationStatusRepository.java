package com.dong.judge.dao.repository;

import com.dong.judge.model.pojo.notification.UserNotificationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户通知状态数据访问接口
 */
@Repository
public interface UserNotificationStatusRepository extends MongoRepository<UserNotificationStatus, String> {
    
    /**
     * 查找指定用户对指定通知的状态
     *
     * @param userId 用户ID
     * @param notificationId 通知ID
     * @return 用户通知状态
     */
    Optional<UserNotificationStatus> findByUserIdAndNotificationId(String userId, String notificationId);
    
    /**
     * 查找指定用户对多个通知的状态
     *
     * @param userId 用户ID
     * @param notificationIds 通知ID列表
     * @return 用户通知状态列表
     */
    List<UserNotificationStatus> findByUserIdAndNotificationIdIn(String userId, List<String> notificationIds);
}