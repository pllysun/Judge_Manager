package com.dong.judge.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dong.judge.model.pojo.user.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}