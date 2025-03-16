package com.dong.judge.config;

import com.dong.judge.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 应用程序启动监听器
 * <p>
 * 在应用程序启动完成后执行一些初始化操作
 * </p>
 */
@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private RoleService roleService;

    /**
     * 应用程序启动完成后执行
     *
     * @param event 应用程序就绪事件
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 初始化超级管理员
        roleService.initSuperAdmin();
    }
}