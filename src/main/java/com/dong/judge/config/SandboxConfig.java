package com.dong.judge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "sandbox")
@Data
public class SandboxConfig {

    private Map<String, LanguageConfig> languages = new HashMap<>();

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Data
    public static class LanguageConfig {
        private String sourceFile;
        private String compileOutFile;
        private List<String> compileCommand;
        private List<String> runCommand;
        private long cpuLimit = 10_000_000_000L; // 10秒
        private long memoryLimit = 256 * 1024 * 1024; // 256MB
        private int procLimit = 50;
        private boolean needCompile = true;
    }
}