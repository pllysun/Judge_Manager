package com.dong.judge.config;

import cn.dev33.satoken.stp.StpInterface;
import com.dong.judge.model.enums.RoleEnum;
import com.dong.judge.model.pojo.user.User;
import com.dong.judge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 自定义权限验证接口扩展
 * <p>
 * 实现Sa-Token的StpInterface接口，自定义权限验证规则
 * </p>
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private UserService userService;

    /**
     * 返回指定账号id所拥有的权限码集合
     *
     * @param loginId   登录用户id，此处为用户邮箱
     * @param loginType 登录类型
     * @return 该用户所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本系统不需要细粒度的权限控制，返回空集合
        return new ArrayList<>();
    }

    /**
     * 返回指定账号id所拥有的角色标识集合
     *
     * @param loginId   登录用户id，此处为用户邮箱
     * @param loginType 登录类型
     * @return 该用户所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 获取用户信息
        User user = userService.getByEmail((String) loginId);
        if (user == null) {
            return new ArrayList<>();
        }
        
        // 获取用户角色
        String roles = user.getRoles();
        if (roles == null || roles.isEmpty()) {
            // 如果没有角色，默认为游客
            return Arrays.asList(RoleEnum.GUEST.getCode());
        }
        
        // 将角色字符串转换为列表
        return Arrays.asList(roles.split(","));
    }
}