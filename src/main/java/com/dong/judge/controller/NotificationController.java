package com.dong.judge.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.dong.judge.model.dto.notification.NotificationRequest;
import com.dong.judge.model.pojo.notification.Notification;
import com.dong.judge.model.pojo.user.User;
import com.dong.judge.model.vo.Result;
import com.dong.judge.service.NotificationService;
import com.dong.judge.service.UserService;
import com.dong.judge.util.UserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统通知控制器
 * <p>
 * 处理系统通知相关的请求，包括创建通知、获取通知列表、标记通知已读等
 * </p>
 */
@RestController
@RequestMapping("/notification")
@Tag(name = "系统通知", description = "系统通知相关接口")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取当前用户的通知列表
     *
     * @return 通知列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取通知列表", description = "获取当前登录用户的通知列表，包括全局通知和用户特定通知")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功获取通知列表"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期")
    })
    public Result<?> getNotificationList() {
        // 检查用户是否已登录
        if (!StpUtil.isLogin()) {
            return Result.error(401, "未登录或登录已过期");
        }

        try {
            String userId=UserUtil.getUserId();
            if (userId == null) {
                return Result.error("用户不存在");
            }
            // 获取通知列表及其状态
            Map<String, Object> notificationsWithStatus = notificationService.getUserNotificationsWithStatus(userId);
            
            return Result.success(notificationsWithStatus);
        } catch (Exception e) {
            return Result.error("获取通知列表失败: " + e.getMessage());
        }
    }
    
    
    /**
     * 标记所有通知为已读
     *
     * @return 操作结果
     */
    @PostMapping("/read-all")
    @Operation(summary = "标记所有通知为已读", description = "将当前用户的所有通知标记为已读状态")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "标记成功"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期")
    })
    public Result<?> markAllAsRead() {
        // 检查用户是否已登录
        if (!StpUtil.isLogin()) {
            return Result.error(401, "未登录或登录已过期");
        }

        try {
            String userId=UserUtil.getUserId();
            if (userId == null) {
                return Result.error("用户不存在");
            }
            
            // 标记所有为已读
            boolean success = notificationService.markAllAsRead(userId);
            
            if (!success) {
                return Result.error("标记所有通知为已读失败");
            }
            
            return Result.success("所有通知已标记为已读");
        } catch (Exception e) {
            return Result.error("标记所有通知为已读失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建系统通知（仅管理员及以上可操作）
     *
     * @param request 通知请求
     * @return 创建结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建系统通知", description = "创建新的系统通知（需要管理员权限）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "通知创建成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @SaCheckRole("ROLE_ADMIN")
    public Result<?> createNotification(@RequestBody @Valid NotificationRequest request) {
        try {
            // 创建通知实体
            Notification notification = new Notification();
            notification.setType(request.getType());
            notification.setTitle(request.getTitle());
            notification.setContent(request.getContent());
            
            // 设置接收者，如果未指定则为全局通知
            String receiverId = request.getReceiverId();
            if (receiverId == null || receiverId.isEmpty()) {
                receiverId = "all"; // 全局通知
            }
            notification.setReceiverId(receiverId);
            
            // 保存通知 - NotificationServiceImpl会处理全局通知的逻辑
            // 如果是全局通知(receiverId="all")，会为每个用户创建独立的通知记录
            // 如果是单个用户的通知，则直接创建一条记录
            Notification createdNotification = notificationService.createNotification(notification);
            
            return Result.success("通知创建成功", createdNotification);
        } catch (Exception e) {
            return Result.error("创建通知失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除通知（仅管理员及以上可操作）
     *
     * @param notificationId 通知ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{notificationId}")
    @Operation(summary = "删除通知", description = "删除指定的系统通知（需要管理员权限）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "通知删除成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @SaCheckRole("ROLE_ADMIN")
    public Result<?> deleteNotification(@PathVariable String notificationId) {
        try {
            // 删除通知
            boolean success = notificationService.deleteNotification(notificationId);
            
            if (!success) {
                return Result.error("删除通知失败，可能通知不存在");
            }
            
            return Result.success("通知删除成功");
        } catch (Exception e) {
            return Result.error("删除通知失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除用户的通知（仅删除当前用户的通知状态，不删除通知本身）
     *
     * @param notificationId 通知ID
     * @return 删除结果
     */
    @DeleteMapping("/delete-user/{notificationId}")
    @Operation(summary = "删除用户通知", description = "删除当前用户的指定通知（仅删除用户通知状态，不删除通知本身）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "通知删除成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期")
    })
    public Result<?> deleteUserNotification(@PathVariable String notificationId) {
        // 检查用户是否已登录
        if (!StpUtil.isLogin()) {
            return Result.error(401, "未登录或登录已过期");
        }

        try {
            // 获取当前用户ID
            String userId = (String) StpUtil.getLoginId();
            
            // 删除用户通知状态
            boolean success = notificationService.deleteUserNotification(userId, notificationId);
            
            if (!success) {
                return Result.error("删除通知失败，可能通知不存在或无权限");
            }
            
            return Result.success("通知删除成功");
        } catch (Exception e) {
            return Result.error("删除通知失败: " + e.getMessage());
        }
    }
    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @return 操作结果
     */
    @PostMapping("/read/{notificationId}")
    @Operation(summary = "标记通知为已读", description = "将指定通知标记为已读状态")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "标记成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期")
    })
    public Result<?> markAsRead(@PathVariable String notificationId) {
        // 检查用户是否已登录
        if (!StpUtil.isLogin()) {
            return Result.error(401, "未登录或登录已过期");
        }

        try {
            // 获取当前用户ID
            String userId = (String) StpUtil.getLoginId();
            
            // 标记为已读
            boolean success = notificationService.markAsRead(notificationId, userId);
            
            if (!success) {
                return Result.error("标记通知为已读失败，可能通知不存在或无权限");
            }
            
            return Result.success("通知已标记为已读");
        } catch (Exception e) {
            return Result.error("标记通知为已读失败: " + e.getMessage());
        }
    }
}
    