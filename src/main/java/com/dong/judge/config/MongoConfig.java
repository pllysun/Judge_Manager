package com.dong.judge.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

/**
 * MongoDB配置类
 * 用于创建MongoTemplate bean，解决Spring Data MongoDB的依赖注入问题
 */
@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        // 从URI中提取数据库名称
        String databaseName = "judge_db";
        if (mongoUri != null && mongoUri.contains("/")) {
            String[] parts = mongoUri.split("/");
            if (parts.length > 0) {
                String lastPart = parts[parts.length - 1];
                // 处理可能的查询参数
                if (lastPart.contains("?")) {
                    lastPart = lastPart.split("\\?")[0];
                }
                if (!lastPart.isEmpty()) {
                    databaseName = lastPart;
                }
            }
        }
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient(), databaseName));
    }
}