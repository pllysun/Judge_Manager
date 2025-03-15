package com.dong.judge.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件存储服务接口
 * <p>
 * 定义文件上传和获取的方法，支持本地存储和云存储
 * </p>
 */
public interface FileStorageService {
    
    /**
     * 上传文件
     *
     * @param file 文件
     * @param userId 用户ID
     * @return 文件访问URL
     * @throws IOException IO异常
     */
    String uploadFile(MultipartFile file, Long userId) throws IOException;
    
    /**
     * 获取文件
     *
     * @param fileName 文件名
     * @return 文件字节数组
     * @throws IOException IO异常
     */
    byte[] getFile(String fileName) throws IOException;
    
    /**
     * 删除文件
     *
     * @param fileName 文件名
     * @return 是否删除成功
     */
    boolean deleteFile(String fileName);
    
    /**
     * 切换存储类型
     *
     * @param storageType 存储类型 (local/aliyun/tencent)
     * @return 是否切换成功
     */
    boolean switchStorageType(String storageType);
    
    /**
     * 同步文件
     * 将文件从当前存储同步到目标存储
     *
     * @param targetStorageType 目标存储类型
     * @return 是否同步成功
     */
    boolean syncFiles(String targetStorageType);
}