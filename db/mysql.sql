CREATE TABLE `user` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                        `username` varchar(20) DEFAULT NULL COMMENT '用户名',
                        `password` varchar(100) NOT NULL COMMENT '密码',
                        `email` varchar(100) NOT NULL COMMENT '邮箱',
                        `nickname` varchar(30) DEFAULT NULL COMMENT '昵称',
                        `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
                        `bio` text DEFAULT NULL COMMENT '个人简介',
                        `created_at` datetime NOT NULL COMMENT '创建时间',
                        `updated_at` datetime NOT NULL COMMENT '更新时间',
                        `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
                        `roles` varchar(255) DEFAULT NULL COMMENT '用户角色',
                        `ban` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否封禁',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_email` (`email`),
                        UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';