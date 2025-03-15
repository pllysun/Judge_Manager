package com.dong.judge.service.impl;

import com.dong.judge.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 本地文件存储服务实现类
 */
@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;
    
    @Override
    public String uploadFile(MultipartFile file, Long userId) throws IOException {
        // 获取文件名
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        
        // 获取文件扩展名
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // 生成新的文件名
        String newFilename = userId + "_" + UUID.randomUUID().toString() + fileExtension;
        
        // 创建目标目录
        String userDir = "avatars/" + userId;
        Path userDirPath = Paths.get(uploadDir, userDir);
        if (!Files.exists(userDirPath)) {
            Files.createDirectories(userDirPath);
        }
        
        // 保存文件
        Path targetPath = userDirPath.resolve(newFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // 返回文件访问路径
        return userDir + "/" + newFilename;
    }
    
    @Override
    public byte[] getFile(String fileName) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
        
        // 检查文件是否存在
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + fileName);
        }
        
        // 读取文件内容
        return Files.readAllBytes(filePath);
    }
    
    @Override
    public boolean deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
    
    @Override
    public boolean switchStorageType(String storageType) {
        // 本地存储实现不需要切换存储类型
        return "local".equals(storageType);
    }
    
    @Override
    public boolean syncFiles(String targetStorageType) {
        // 本地存储实现不需要同步文件
        return true;
    }
}