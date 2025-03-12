package com.dong.judge.service;

public interface EmailService {
    boolean sendVerificationEmail(String toEmail, String code);
}