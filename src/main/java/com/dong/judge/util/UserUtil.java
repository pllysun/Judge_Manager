package com.dong.judge.util;


import cn.dev33.satoken.stp.StpUtil;
import com.dong.judge.model.pojo.user.User;
import com.dong.judge.model.vo.Result;
import com.dong.judge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户工具类
 * <p>
 * 提供用户相关的工具方法，如获取当前登录用户ID等
 * </p>
 */
@Component
public class UserUtil {

    private static UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        UserUtil.userService = userService;
    }

    /**
     * 获取当前登录用户的ID
     *
     * @return 用户ID，如果获取失败则返回null
     */
    public static Result<String> getUserIdByLoginEmail() {
        // 检查用户是否已登录
        if (!StpUtil.isLogin()) {
            return Result.error(401, "未登录或登录已过期");
        }

        try {
            // 获取当前用户邮箱
            String email = (String) StpUtil.getLoginId();
            // 获取用户信息
            User user = userService.getByEmail(email);
            if (user == null) {
                return Result.error("用户不存在");
            }
            // 获取用户ID
            String userId = user.getId().toString();

            return Result.success(userId);
        } catch (Exception e) {
            return Result.error("获取用户ID失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户的用户ID
     *
     * @return 返回当前登录用户的用户ID字符串，如果用户不存在则返回null
     */
    public  static  String  getUserId(){
        // 获取当前用户邮箱
        String email = (String) StpUtil.getLoginId();
        // 获取用户信息
        User user = userService.getByEmail(email);
        if (user == null) {
            return null;
        }else{
            return user.getId().toString();
        }
    }

    /**
     * 根据邮箱获取用户ID
     *
     * @param email 用户邮箱
     * @return 用户ID字符串，如果用户不存在则返回null
     */
    public  static  String  getUserId(String email){
        // 获取用户信息
        User user = userService.getByEmail(email);
        if (user == null) {
            return null;
        }else{
            return user.getId().toString();
        }
    }

}
