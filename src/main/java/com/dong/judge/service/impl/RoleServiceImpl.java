package com.dong.judge.service.impl;

import com.dong.judge.model.enums.RoleEnum;
import com.dong.judge.model.pojo.user.User;
import com.dong.judge.service.RoleService;
import com.dong.judge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 * <p>
 * 实现用户角色管理相关的功能
 * </p>
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private UserService userService;

    /**
     * 为用户设置角色（替换原有角色）
     *
     * @param user  用户对象
     * @param roles 角色枚举数组
     * @return 是否设置成功
     */
    @Override
    public boolean setUserRoles(User user, RoleEnum... roles) {
        if (user == null || roles == null || roles.length == 0) {
            return false;
        }

        // 获取最高角色等级
        int highestLevel = 0;
        for (RoleEnum role : roles) {
            if (role.getLevel() > highestLevel) {
                highestLevel = role.getLevel();
            }
        }

        // 根据最高角色等级获取所有应该拥有的角色编码
        String[] roleCodes = RoleEnum.getRoleCodesByLevel(highestLevel);
        String rolesStr = String.join(",", roleCodes);

        // 设置用户角色
        user.setRoles(rolesStr);
        return userService.updateById(user);
    }

    /**
     * 为用户设置角色（替换原有角色）
     *
     * @param userId 用户ID
     * @param roles  角色枚举数组
     * @return 是否设置成功
     */
    @Override
    public boolean setUserRoles(Long userId, RoleEnum... roles) {
        User user = userService.getById(userId);
        if (user == null) {
            return false;
        }
        return setUserRoles(user, roles);
    }

    /**
     * 为用户添加角色（保留原有角色）
     *
     * @param user  用户对象
     * @param roles 角色枚举数组
     * @return 是否添加成功
     */
    @Override
    public boolean addUserRoles(User user, RoleEnum... roles) {
        if (user == null || roles == null || roles.length == 0) {
            return false;
        }

        // 获取用户当前角色
        Set<String> currentRoles = new HashSet<>();
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            currentRoles.addAll(Arrays.asList(user.getRoles().split(",")));
        }

        // 添加新角色
        for (RoleEnum role : roles) {
            // 获取该角色及其以下的所有角色
            String[] roleCodes = RoleEnum.getRoleCodesByLevel(role.getLevel());
            currentRoles.addAll(Arrays.asList(roleCodes));
        }

        // 更新用户角色
        user.setRoles(String.join(",", currentRoles));
        return userService.updateById(user);
    }

    /**
     * 为用户添加角色（保留原有角色）
     *
     * @param userId 用户ID
     * @param roles  角色枚举数组
     * @return 是否添加成功
     */
    @Override
    public boolean addUserRoles(Long userId, RoleEnum... roles) {
        User user = userService.getById(userId);
        if (user == null) {
            return false;
        }
        return addUserRoles(user, roles);
    }

    /**
     * 移除用户角色
     *
     * @param user  用户对象
     * @param roles 角色枚举数组
     * @return 是否移除成功
     */
    @Override
    public boolean removeUserRoles(User user, RoleEnum... roles) {
        if (user == null || roles == null || roles.length == 0 || user.getRoles() == null || user.getRoles().isEmpty()) {
            return false;
        }

        // 获取用户当前角色
        Set<String> currentRoles = new HashSet<>(Arrays.asList(user.getRoles().split(",")));

        // 要移除的角色编码
        Set<String> rolesToRemove = Arrays.stream(roles)
                .map(RoleEnum::getCode)
                .collect(Collectors.toSet());

        // 移除指定角色
        currentRoles.removeAll(rolesToRemove);

        // 如果移除后没有角色，则至少保留GUEST角色
        if (currentRoles.isEmpty()) {
            currentRoles.add(RoleEnum.GUEST.getCode());
        }

        // 更新用户角色
        user.setRoles(String.join(",", currentRoles));
        return userService.updateById(user);
    }

    /**
     * 移除用户角色
     *
     * @param userId 用户ID
     * @param roles  角色枚举数组
     * @return 是否移除成功
     */
    @Override
    public boolean removeUserRoles(Long userId, RoleEnum... roles) {
        User user = userService.getById(userId);
        if (user == null) {
            return false;
        }
        return removeUserRoles(user, roles);
    }

    /**
     * 判断用户是否拥有指定角色
     *
     * @param user 用户对象
     * @param role 角色枚举
     * @return 是否拥有该角色
     */
    @Override
    public boolean hasRole(User user, RoleEnum role) {
        if (user == null || role == null || user.getRoles() == null || user.getRoles().isEmpty()) {
            return false;
        }

        String[] userRoles = user.getRoles().split(",");
        for (String userRole : userRoles) {
            if (userRole.equals(role.getCode())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断用户是否拥有指定角色
     *
     * @param userId 用户ID
     * @param role   角色枚举
     * @return 是否拥有该角色
     */
    @Override
    public boolean hasRole(Long userId, RoleEnum role) {
        User user = userService.getById(userId);
        if (user == null) {
            return false;
        }
        return hasRole(user, role);
    }

    /**
     * 获取用户的最高角色
     *
     * @param user 用户对象
     * @return 最高角色枚举
     */
    @Override
    public RoleEnum getHighestRole(User user) {
        if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
            return RoleEnum.GUEST;
        }

        String[] userRoles = user.getRoles().split(",");
        RoleEnum highestRole = RoleEnum.GUEST;

        for (String roleCode : userRoles) {
            RoleEnum role = RoleEnum.getByCode(roleCode);
            if (role != null && role.getLevel() > highestRole.getLevel()) {
                highestRole = role;
            }
        }

        return highestRole;
    }

    /**
     * 获取用户的最高角色
     *
     * @param userId 用户ID
     * @return 最高角色枚举
     */
    @Override
    public RoleEnum getHighestRole(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return RoleEnum.GUEST;
        }
        return getHighestRole(user);
    }

    /**
     * 初始化超级管理员
     * 将ID为1的用户设置为超级管理员
     */
    @Override
    public void initSuperAdmin() {
        User admin = userService.getById(1L);
        if (admin != null) {
            setUserRoles(admin, RoleEnum.SUPER_ADMIN);
        }
    }
}