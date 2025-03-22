package com.dong.judge.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cn.dev33.satoken.stp.StpUtil;

import com.dong.judge.service.NotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class NotificationControllerTest{

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
    }

    @Test
    public void testMarkAllAsReadWhenNotLoggedInShouldReturnUnauthorized() throws Exception {
        try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::isLogin).thenReturn(false);
    
            mockMvc.perform(post("/notification/read-all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("未登录或登录已过期"));
        }
    }

    @Test
    public void testMarkAllAsReadWhenLoggedInAndSuccessShouldReturnSuccess() throws Exception {
        try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
            stpUtilMock.when(StpUtil::getLoginId).thenReturn("user123");
    
            when(notificationService.markAllAsRead("user123")).thenReturn(true);
    
            mockMvc.perform(post("/notification/read-all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("所有通知已标记为已读"));
        }
    }

    @Test
    public void testMarkAllAsReadWhenLoggedInAndFailureShouldReturnError() throws Exception {
        try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
            stpUtilMock.when(StpUtil::getLoginId).thenReturn("user123");
    
            when(notificationService.markAllAsRead("user123")).thenReturn(false);
    
            mockMvc.perform(post("/notification/read-all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("标记所有通知为已读失败"));
        }
    }

    @Test
    public void testMarkAllAsReadWhenExceptionThrownShouldReturnError() throws Exception {
        try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
            stpUtilMock.when(StpUtil::getLoginId).thenReturn("user123");
    
            when(notificationService.markAllAsRead("user123")).thenThrow(new RuntimeException("Database error"));
    
            mockMvc.perform(post("/notification/read-all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("标记所有通知为已读失败: Database error"));
        }
    }

}