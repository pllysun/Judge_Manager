package com.dong.judge.service.impl;

    import com.dong.judge.config.SandboxConfig;
    import com.dong.judge.model.dto.sandbox.CodeExecuteRequest;
    import com.dong.judge.model.dto.sandbox.CompileRequest;
    import com.dong.judge.model.dto.sandbox.RunRequest;
    import com.dong.judge.model.vo.sandbox.CodeExecuteResult;
    import com.dong.judge.model.vo.sandbox.CompileResult;
    import com.dong.judge.model.vo.sandbox.RunResult;
    import com.dong.judge.service.SandboxService;
    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.fasterxml.jackson.databind.node.ArrayNode;
    import com.fasterxml.jackson.databind.node.ObjectNode;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.http.HttpEntity;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.HttpMethod;
    import org.springframework.http.ResponseEntity;
    import org.springframework.stereotype.Service;
    import org.springframework.web.client.RestTemplate;

    @Service
    @Slf4j
    public class SandboxServiceImpl implements SandboxService {

        @Value("${sandbox.api.url}")
        private String sandboxApiUrl;

        @Autowired
        private RestTemplate restTemplate;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private SandboxConfig sandboxConfig;

        @Override
        public CodeExecuteResult executeCode(CodeExecuteRequest request) {
            try {
                // 1. 获取语言配置（支持别名）
                SandboxConfig.LanguageConfig langConfig = sandboxConfig.getLanguageConfig(request.getLanguage());
                if (langConfig == null) {
                    return buildErrorResult("不支持的语言: " + request.getLanguage());
                }

                // 2. 编译代码（如果需要）
                String fileId = null;
                if (langConfig.isNeedCompile()) {
                    CompileRequest compileRequest = CompileRequest.builder()
                            .code(request.getCode())
                            .language(request.getLanguage())
                            .build();

                    CompileResult compileResult = compileCode(compileRequest);

                    // 检查编译���果
                    if (!compileResult.isSuccess()) {
                        return CodeExecuteResult.builder()
                                .status("Compile Error")
                                .stdout("")
                                .stderr(compileResult.getErrorMessage())
                                .compileError(compileResult.getErrorMessage())
                                .build();
                    }

                    fileId = compileResult.getFileId();
                }

                // 3. 运行代码
                RunRequest runRequest = RunRequest.builder()
                        .code(request.getCode())
                        .language(request.getLanguage())
                        .input(request.getInput())
                        .fileId(fileId)
                        .build();

                RunResult runResult = runCode(runRequest);

                // 4. 处理运行结果
                CodeExecuteResult result = CodeExecuteResult.builder()
                        .status(runResult.getStatus())
                        .exitStatus(runResult.getExitStatus())
                        .time(runResult.getTime())
                        .memory(runResult.getMemory())
                        .runTime(runResult.getRunTime())
                        .stdout(runResult.getStdout())
                        .stderr(runResult.getStderr())
                        .build();

                // 5. 清理资源
                if (fileId != null) {
                    deleteFile(fileId);
                }

                return result;

            } catch (Exception e) {
                log.error("执行代码时发生错误", e);
                return buildErrorResult("执行代码时发生错误: " + e.getMessage());
            }
        }

        @Override
        public CompileResult compileCode(CompileRequest request) {
            try {
                // 1. 获取语言配置（支持别名）
                SandboxConfig.LanguageConfig langConfig = sandboxConfig.getLanguageConfig(request.getLanguage());
                if (langConfig == null) {
                    return CompileResult.builder()
                            .success(false)
                            .isCompile(false)
                            .errorMessage("不支持的语言: " + request.getLanguage())
                            .build();
                }

                // 2. 如果是解释型语言，不需要编译
                if (!langConfig.isNeedCompile()) {
                    return CompileResult.builder()
                            .isCompile(false)
                            .success(true)
                            .build();
                }

                // 3. 发送编译请求
                JsonNode compileResponse = sendCompileRequest(request.getCode(), langConfig);

                // 4. 处理编译结果
                if (compileResponse == null || !compileResponse.isArray() || compileResponse.isEmpty()) {
                    return CompileResult.builder()
                            .isCompile(true)
                            .success(false)
                            .errorMessage("编译服务返回无效响应")
                            .build();
                }

                JsonNode firstResult = compileResponse.get(0);
                String status = firstResult.path("status").asText();

                if (!"Accepted".equals(status)) {
                    return CompileResult.builder()
                            .isCompile(true)
                            .success(false)
                            .errorMessage(firstResult.path("files").path("stderr").asText())
                            .status(status)
                            .build();
                }

                // 获取编译后的文件ID
                String fileId = firstResult.path("fileIds").path(langConfig.getCompileOutFile()).asText();

                return CompileResult.builder()
                        .isCompile(true)
                        .success(true)
                        .fileId(fileId)
                        .status(status)
                        .build();

            } catch (Exception e) {
                log.error("编译代码时发生错误", e);
                return CompileResult.builder()
                        .isCompile(true)
                        .success(false)
                        .errorMessage("编译代码时发生错误: " + e.getMessage())
                        .build();
            }
        }

        @Override
        public RunResult runCode(RunRequest request) {
            try {
                // 1. 获取语言配置（支持别名）
                SandboxConfig.LanguageConfig langConfig = sandboxConfig.getLanguageConfig(request.getLanguage());
                if (langConfig == null) {
                    return RunResult.builder()
                            .status("Error")
                            .stderr("不支持的语言: " + request.getLanguage())
                            .build();
                }

                // 2. 发送运行请求
                JsonNode runResponse = sendRunRequest(request.getInput(), request.getFileId(),
                        request.getCode(), langConfig);

                // 3. 处理运行结果
                if (runResponse == null || !runResponse.isArray() || runResponse.isEmpty()) {
                    return RunResult.builder()
                            .status("Error")
                            .stderr("运行服务返回无效响应")
                            .build();
                }

                JsonNode firstResult = runResponse.get(0);

                return RunResult.builder()
                        .status(firstResult.path("status").asText())
                        .exitStatus(firstResult.path("exitStatus").asInt())
                        .time(firstResult.path("time").asLong())
                        .memory(firstResult.path("memory").asLong())
                        .runTime(firstResult.path("runTime").asLong())
                        .stdout(firstResult.path("files").path("stdout").asText())
                        .stderr(firstResult.path("files").path("stderr").asText())
                        .build();

            } catch (Exception e) {
                log.error("运行代码时发生错误", e);
                return RunResult.builder()
                        .status("Error")
                        .stderr("运行代码时发生错误: " + e.getMessage())
                        .build();
            }
        }

        private JsonNode sendCompileRequest(String code, SandboxConfig.LanguageConfig langConfig) throws Exception {
            // 构建编译请求
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode cmdArray = requestBody.putArray("cmd");

            ObjectNode cmd = cmdArray.addObject();

            // 设置编译命令
            ArrayNode args = cmd.putArray("args");
            for (String arg : langConfig.getCompileCommand()) {
                args.add(arg);
            }

            // 设置环境变量
            ArrayNode env = cmd.putArray("env");
            env.add("PATH=/usr/bin:/bin");

            // 设置文件
            ArrayNode files = cmd.putArray("files");

            // 标准输入（空）
            files.addObject().put("content", "");

            // 标准输出
            ObjectNode stdout = files.addObject();
            stdout.put("name", "stdout");
            stdout.put("max", 10240);

            // 标准错误
            ObjectNode stderr = files.addObject();
            stderr.put("name", "stderr");
            stderr.put("max", 10240);

            // 设置资源限制
            cmd.put("cpuLimit", langConfig.getCpuLimit());
            cmd.put("memoryLimit", langConfig.getMemoryLimit());
            cmd.put("procLimit", langConfig.getProcLimit());

            // 设置输入文件
            ObjectNode copyIn = cmd.putObject("copyIn");
            ObjectNode sourceFile = copyIn.putObject(langConfig.getSourceFile());
            sourceFile.put("content", code);

            // 设置输出和缓存文件
            ArrayNode copyOut = cmd.putArray("copyOut");
            copyOut.add("stdout");
            copyOut.add("stderr");

            ArrayNode copyOutCached = cmd.putArray("copyOutCached");
            copyOutCached.add(langConfig.getCompileOutFile());

            // 发送编译请求
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    sandboxApiUrl + "/run",
                    HttpMethod.POST,
                    entity,
                    JsonNode.class);

            return response.getBody();
        }

        private JsonNode sendRunRequest(String input, String fileId, String code, SandboxConfig.LanguageConfig langConfig) throws Exception {
            // 构建运行请求
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode cmdArray = requestBody.putArray("cmd");

            ObjectNode cmd = cmdArray.addObject();

            // 设置运行命令
            ArrayNode args = cmd.putArray("args");
            if (fileId != null) {
                // 编译型语言
                args.add(langConfig.getCompileOutFile());
            } else {
                // 解释型语言
                for (String arg : langConfig.getRunCommand()) {
                    args.add(arg);
                }
            }

            // 设置环境变量
            ArrayNode env = cmd.putArray("env");
            env.add("PATH=/usr/bin:/bin");

            // 设置文件
            ArrayNode files = cmd.putArray("files");

            // 标准输入
            files.addObject().put("content", input != null ? input : "");

            // 标准输出
            ObjectNode stdout = files.addObject();
            stdout.put("name", "stdout");
            stdout.put("max", 10240);

            // 标准错误
            ObjectNode stderr = files.addObject();
            stderr.put("name", "stderr");
            stderr.put("max", 10240);

            // 设置资源限制
            cmd.put("cpuLimit", langConfig.getCpuLimit());
            cmd.put("memoryLimit", langConfig.getMemoryLimit());
            cmd.put("procLimit", langConfig.getProcLimit());

            // 设置输入文件
            ObjectNode copyIn = cmd.putObject("copyIn");

            if (fileId != null) {
                // 编译型语言，使用编译后的文件
                ObjectNode execFile = copyIn.putObject(langConfig.getCompileOutFile());
                execFile.put("fileId", fileId);
            } else {
                // 解释型语言，直接使用源代码
                ObjectNode sourceFile = copyIn.putObject(langConfig.getSourceFile());
                sourceFile.put("content", code);
            }

            // 发送运行请求
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    sandboxApiUrl + "/run",
                    HttpMethod.POST,
                    entity,
                    JsonNode.class);

            return response.getBody();
        }

        @Override
        public void deleteFile(String fileId) {
            try {
                if (fileId != null && !fileId.isEmpty()) {
                    restTemplate.delete(sandboxApiUrl + "/file/" + fileId);
                    log.debug("删除文件成功: {}", fileId);
                }
            } catch (Exception e) {
                log.error("删除文件失败: {}", fileId, e);
            }
        }

        private CodeExecuteResult buildErrorResult(String message) {
            return CodeExecuteResult.builder()
                    .status("Error")
                    .exitStatus(-1)
                    .stdout("")
                    .stderr(message)
                    .build();
        }
    }