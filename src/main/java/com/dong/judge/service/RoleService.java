package com.dong.judge.service;

import com.dong.judge.model.enums.RoleEnum;
import com.dong.judge.model.pojo.user.User;

/**
 * 角色服务接口
 * <p>
 * 提供用户角色管理相关的功能
 * </p>
 */
public interface RoleService {
    
    /**
     * 为用户设置角色
     *
     * @param user  用户对象
     * @param roles 角色枚举数组
     * @return 是否设置成功
     */
    boolean setUserRoles(User user, RoleEnum... roles);
    
    /**
     * 为用户设置角色
     *
     * @param userId 用户ID
     * @param roles  角色枚举数组
     * @return 是否设置成功
     */
    boolean setUserRoles(Long userId, RoleEnum... roles);
    
    /**
     * 为用户添加角色
     *
     * @param user  用户对象
     * @param roles 角色枚举数组
     * @return 是否添加成功
     */
    boolean addUserRoles(User user, RoleEnum... roles);
    
    /**
     * 为用户添加角色
     *
     * @param userId 用户ID
     * @param roles  角色枚举数组
     * @return 是否添加成功
     */
    boolean addUserRoles(Long userId, RoleEnum... roles);
    
    /**
     * 移除用户角色
     *
     * @param user  用户对象
     * @param roles 角色枚举数组
     * @return 是否移除成功
     */
    boolean removeUserRoles(User user, RoleEnum... roles);
    
    /**
     * 移除用户角色
     *
     * @param userId 用户ID
     * @param roles  角色枚举数组
     * @return 是否移除成功
     */
    boolean removeUserRoles(Long userId, RoleEnum... roles);
    
    /**
     * 判断用户是否拥有指定角色
     *
     * @param user 用户对象
     * @param role 角色枚举
     * @return 是否拥有该角色
     */
    boolean hasRole(User user, RoleEnum role);
    
    /**
     * 判断用户是否拥有指定角色
     *
     * @param userId 用户ID
     * @param role   角色枚举
     * @return 是否拥有该角色
     */
    boolean hasRole(Long userId, RoleEnum role);
    
    /**
     * 获取用户的最高角色
     *
     * @param user 用户对象
     * @return 最高角色枚举
     */
    RoleEnum getHighestRole(User user);
    
    /**
     * 获取用户的最高角色
     *
     * @param userId 用户ID
     * @return 最高角色枚举
     */
    RoleEnum getHighestRole(Long userId);
    
    /**
     * 初始化超级管理员
     * 将ID为1的用户设置为超级管理员
     */
    void initSuperAdmin();
}