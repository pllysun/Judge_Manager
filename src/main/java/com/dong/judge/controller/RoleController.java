package com.dong.judge.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.dong.judge.model.dto.user.RoleUpdateRequest;
import com.dong.judge.model.enums.RoleEnum;
import com.dong.judge.model.pojo.user.User;
import com.dong.judge.model.vo.Result;
import com.dong.judge.service.RoleService;
import com.dong.judge.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 角色管理控制器
 * <p>
 * 提供角色管理相关的API接口
 * </p>
 */
@RestController
@RequestMapping("/role")
@Tag(name = "角色管理", description = "角色管理相关接口")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户角色
     *
     * @return 角色信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前用户角色", description = "获取当前登录用户的角色信息")
    public Result<?> getCurrentUserRole() {
        if (!StpUtil.isLogin()) {
            return Result.error(401, "未登录或登录已过期");
        }

        try {
            // 获取当前用户
            String email = (String) StpUtil.getLoginId();
            User user = userService.getByEmail(email);

            if (user == null) {
                return Result.error(400, "用户不存在");
            }

            // 获取用户最高角色
            RoleEnum highestRole = roleService.getHighestRole(user);

            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("roleCode", highestRole.getCode());
            data.put("roleName", highestRole.getName());
            data.put("roleLevel", highestRole.getLevel());

            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取角色信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户角色（仅管理员及以上可操作）
     *
     * @param request 角色更新请求
     * @return 更新结果
     */
    @PostMapping("/update")
    @Operation(summary = "更新用户角色", description = "更新指定用户的角色（需要管理员权限）")
    @SaCheckRole("ROLE_ADMIN")
    public Result<?> updateUserRole(@RequestBody @Valid RoleUpdateRequest request) {
        try {
            // 获取当前用户角色
            String email = (String) StpUtil.getLoginId();
            User currentUser = userService.getByEmail(email);
            RoleEnum currentUserRole = roleService.getHighestRole(currentUser);

            // 获取目标用户
            User targetUser = userService.getById(request.getUserId());
            if (targetUser == null) {
                return Result.error(400, "目标用户不存在");
            }

            // 获取要设置的角色
            RoleEnum targetRole = RoleEnum.getByCode(request.getRoleCode());
            if (targetRole == null) {
                return Result.error(400, "无效的角色编码");
            }

            // 权限检查：管理员不能设置超级管理员角色
            if (targetRole == RoleEnum.SUPER_ADMIN && currentUserRole != RoleEnum.SUPER_ADMIN) {
                return Result.error(403, "只有超级管理员才能授予超级管理员权限");
            }

            // 权限检查：不能设置比自己高的角色
            if (targetRole.getLevel() > currentUserRole.getLevel()) {
                return Result.error(403, "不能设置比自己高的角色");
            }

            // 更新用户角色
            boolean success = roleService.setUserRoles(targetUser, targetRole);
            if (!success) {
                return Result.error("角色更新失败");
            }

            return Result.success("角色更新成功");
        } catch (Exception e) {
            return Result.error("角色更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有角色列表（仅管理员及以上可操作）
     *
     * @return 角色列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有角色", description = "获取系统中所有可用的角色列表（需要管理员权限）")
    @SaCheckRole("ROLE_ADMIN")
    public Result<?> getAllRoles() {
        try {
            RoleEnum[] roles = RoleEnum.values();
            
            // 获取当前用户角色
            String email = (String) StpUtil.getLoginId();
            User currentUser = userService.getByEmail(email);
            RoleEnum currentUserRole = roleService.getHighestRole(currentUser);
            
            // 构建返回数据，只返回当前用户角色等级及以下的角色
            Map<String, Object> data = new HashMap<>();
            data.put("roles", roles);
            data.put("currentUserRoleLevel", currentUserRole.getLevel());
            
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取角色列表失败: " + e.getMessage());
        }
    }
}