package com.dong.judge.config;

import com.dong.judge.service.FileStorageService;
import com.dong.judge.service.impl.CloudFileStorageServiceImpl;
import com.dong.judge.service.impl.LocalFileStorageServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 文件存储配置类
 * <p>
 * 根据配置属性动态选择使用哪个存储服务实现
 * </p>
 */
@Configuration
public class FileStorageConfig {

    /**
     * 本地存储服务
     * 当file.storage.type为local或未配置时使用本地存储
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "file.storage.type", havingValue = "local", matchIfMissing = true)
    public FileStorageService localFileStorageService() {
        return new LocalFileStorageServiceImpl();
    }

    /**
     * 云存储服务
     * 当file.storage.type为aliyun或tencent时使用云存储
     */
    @Bean
    @ConditionalOnProperty(name = "file.storage.type", havingValue = "aliyun")
    public FileStorageService aliyunFileStorageService() {
        return new CloudFileStorageServiceImpl();
    }

    /**
     * 云存储服务
     * 当file.storage.type为tencent时使用云存储
     */
    @Bean
    @ConditionalOnProperty(name = "file.storage.type", havingValue = "tencent")
    public FileStorageService tencentFileStorageService() {
        return new CloudFileStorageServiceImpl();
    }
}