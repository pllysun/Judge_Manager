package com.dong.judge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;

@Configuration
@ConfigurationProperties(prefix = "sandbox")
@Data
public class SandboxConfig {

    private Map<String, LanguageConfig> languages = new HashMap<>();
    
    // 语言别名映射表，存储所有语言标识符和别名到标准语言标识符的映射
    private final Map<String, String> normalizedLanguageMap = new HashMap<>();

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    /**
     * 初始化语言别名映射
     * 直接在代码中硬编码添加语言别名，避免配置文件解析问题
     */
    @PostConstruct
    public void initLanguageAliasMap() {
        
        // 首先添加所有标准语言标识符到映射表
        for (String lang : languages.keySet()) {
            normalizedLanguageMap.put(lang.toLowerCase(), lang);
        }
        
        // 查找cpp语言配置
        String cppLang = null;
        String javaLang = null;
        String pythonLang = null;
        
        // 查找各语言的标准标识符
        for (String lang : languages.keySet()) {
            if (lang.equalsIgnoreCase("cpp")) {
                cppLang = lang;
            } else if (lang.equalsIgnoreCase("java")) {
                javaLang = lang;
            } else if (lang.equalsIgnoreCase("python")) {
                pythonLang = lang;
            }
        }
        
        // 硬编码添加C/C++语言别名
        if (cppLang != null) {
            // C++的别名
            normalizedLanguageMap.put("C++", cppLang);
            normalizedLanguageMap.put("c++", cppLang);
            // C的别名
            normalizedLanguageMap.put("C", cppLang);
            normalizedLanguageMap.put("c", cppLang);
        }
        
        // 硬编码添加Java语言别名
        if (javaLang != null) {
            normalizedLanguageMap.put("JAVA", javaLang);
            normalizedLanguageMap.put("Java", javaLang);
            normalizedLanguageMap.put("java8", javaLang);
            normalizedLanguageMap.put("java11", javaLang);
            normalizedLanguageMap.put("java17", javaLang);
        }
        
        // 硬编码添加Python语言别名
        if (pythonLang != null) {
            normalizedLanguageMap.put("py", pythonLang);
            normalizedLanguageMap.put("py3", pythonLang);
            normalizedLanguageMap.put("python3", pythonLang);
            normalizedLanguageMap.put("Python", pythonLang);
            normalizedLanguageMap.put("Python3", pythonLang);
        }
    }
    
    /**
     * 根据语言标识符或别名获取标准语言配置
     * 
     * @param language 语言标识符或别名
     * @return 语言配置，如果不存在则返回null
     */
    public LanguageConfig getLanguageConfig(String language) {
        if (language == null) {
            return null;
        }
        
        // 打印调试信息，查看传入的语言标识符
        System.out.println("获取语言配置: '" + language + "'");
        
        // 尝试从映射表中获取标准语言标识符
        String normalizedLang = normalizedLanguageMap.get(language.toLowerCase());
        if (normalizedLang != null) {
            return languages.get(normalizedLang);
        }
        
        // 如果映射表中不存在，直接尝试从languages中获取
        return languages.get(language);
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