package com.dong.judge.service.impl;

import com.dong.judge.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.nio.charset.StandardCharsets;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String VERIFICATION_TEMPLATE_PATH = "classpath:templates/email/verification.html";

    @Override
    public boolean sendVerificationEmail(String toEmail, String code) {
        try {
            // 读取HTML模板文件
            Resource resource = new ClassPathResource(VERIFICATION_TEMPLATE_PATH.replace("classpath:", ""));
            String templateContent = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);

            // 替换模板中的占位符
            String emailContent = templateContent.replace("{{code}}", code);

            // 创建MimeMessage
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Judge System - 邮箱验证码");
            helper.setText(emailContent, true); // 第二个参数true表示启用HTML

            mailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}