package com.dong.judge.dao.repository;

import com.dong.judge.model.pojo.notification.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 通知数据访问接口
 * <p>
 * 处理Notification实体的数据访问
 * </p>
 */
@Repository
public interface NotificationCombinedRepository extends MongoRepository<Notification, String> {
    
    /**
     * 查找指定接收者的通知
     *
     * @param receiverId 接收者ID
     * @return 通知列表
     */
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(String receiverId);
    
    /**
     * 查找全局通知和指定接收者的通知
     *
     * @param receiverIds 全局接收者ID
     * @return 通知列表
     */
    List<Notification> findByReceiverIdInOrderByCreatedAtDesc(List<String> receiverIds);
    
    /**
     * 统计用户未读通知数量
     *
     * @param receiverIds 接收者ID列表
     * @param read 是否已读
     * @return 未读通知数量
     */
    long countByReceiverIdInAndRead(List<String> receiverIds, boolean read);
}