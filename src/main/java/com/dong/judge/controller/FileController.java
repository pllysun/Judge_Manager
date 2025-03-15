package com.dong.judge.controller;

import com.dong.judge.model.vo.Result;
import com.dong.judge.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 文件控制器
 * <p>
 * 处理文件上传、下载和存储类型切换等操作
 * </p>
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;
    
    @Value("${file.storage.type:local}")
    private String storageType;
    
    /**
     * 获取文件
     *
     * @param fileName 文件名
     * @return 文件内容
     */
    @GetMapping("/avatar/{fileName:.+}")
    @Operation(summary = "获取用户头像", description = "根据文件名获取用户头像文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取文件"),
        @ApiResponse(responseCode = "404", description = "文件不存在")
    })
    public ResponseEntity<?> getAvatar(@PathVariable String fileName) {
        try {
            // 如果是云存储且文件名是完整URL，则重定向到该URL
            if (("aliyun".equals(storageType) || "tencent".equals(storageType)) && 
                (fileName.startsWith("http://") || fileName.startsWith("https://"))) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, fileName)
                        .build();
            }
            
            // 获取文件内容
            byte[] fileContent = fileStorageService.getFile("avatars/" + fileName);
            
            // 根据文件扩展名设置Content-Type
            String contentType = "image/jpeg"; // 默认JPEG
            if (fileName.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (fileName.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            } else if (fileName.toLowerCase().endsWith(".bmp")) {
                contentType = "image/bmp";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileContent);
            
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 切换存储类型
     *
     * @param storageType 存储类型 (local/aliyun/tencent)
     * @return 切换结果
     */
    @PostMapping("/switchStorage")
    @Operation(summary = "切换存储类型", description = "切换文件存储类型，支持本地存储和云存储")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "切换成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public Result<?> switchStorageType(@RequestParam String storageType) {
        if (!"local".equals(storageType) && !"aliyun".equals(storageType) && !"tencent".equals(storageType)) {
            return Result.error(400, "不支持的存储类型，支持的类型：local, aliyun, tencent");
        }
        
        try {
            // 切换存储类型
            boolean switched = fileStorageService.switchStorageType(storageType);
            if (!switched) {
                return Result.error("切换存储类型失败");
            }
            
            // 同步文件
            boolean synced = fileStorageService.syncFiles(storageType);
            if (!synced) {
                return Result.error("文件同步失败，请手动同步");
            }
            
            return Result.success("切换存储类型成功");
            
        } catch (Exception e) {
            return Result.error("切换存储类型失败: " + e.getMessage());
        }
    }
}