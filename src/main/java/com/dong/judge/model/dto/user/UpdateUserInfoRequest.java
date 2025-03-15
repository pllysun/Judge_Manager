package com.dong.judge.model.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateUserInfoRequest {
    
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    @Size(max = 30, message = "昵称长度不能超过30个字符")
    private String nickname;
    
    private MultipartFile avatarFile;
    
    private String avatar;
    
    private String bio;
}