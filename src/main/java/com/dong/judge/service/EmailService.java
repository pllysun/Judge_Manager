package com.dong.judge.service;

public interface EmailService {
    boolean sendVerificationEmail(String toEmail, String code);

    boolean sendPasswordResetEmail(String email, String code);

    /**
     * 发送系统通知邮件
     *
     * @param email 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 是否发送成功
     */
    boolean sendSystemNotification(String email, String subject, String content);
}