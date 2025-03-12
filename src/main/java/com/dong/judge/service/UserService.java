package com.dong.judge.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.dong.judge.model.pojo.user.User;

public interface UserService extends IService<User> {
    boolean isEmailExist(String email);
    boolean isUsernameExist(String username);

    User getByEmail(String email);
}
