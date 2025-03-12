package com.dong.judge.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.judge.dao.mapper.UserMapper;
import com.dong.judge.model.pojo.user.User;
import com.dong.judge.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public boolean isEmailExist(String email) {
        return this.count(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) > 0;
    }

    @Override
    public boolean isUsernameExist(String username) {
        return this.count(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0;
    }

    @Override
    public User getByEmail(String email) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
    }
}
