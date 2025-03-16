package com.dong.judge.service.impl;

import com.dong.judge.dao.repository.ProblemRepository;
import com.dong.judge.dao.repository.TestGroupRepository;
import com.dong.judge.model.dto.code.StandardCodeRequest;
import com.dong.judge.model.dto.code.TestCase;
import com.dong.judge.model.dto.sandbox.CodeExecuteRequest;
import com.dong.judge.model.dto.sandbox.CompileRequest;
import com.dong.judge.model.dto.sandbox.RunRequest;
import com.dong.judge.model.pojo.judge.Problem;
import com.dong.judge.model.pojo.judge.TestGroup;
import com.dong.judge.model.vo.sandbox.CodeExecuteResult;
import com.dong.judge.model.vo.sandbox.CompileResult;
import com.dong.judge.model.vo.sandbox.RunResult;
import com.dong.judge.service.SandboxService;
import com.dong.judge.service.TestGroupService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;



@Slf4j
@Service
@RequiredArgsConstructor
public class TestGroupServiceImpl implements TestGroupService {

    private final TestGroupRepository testGroupRepository;
    private final ProblemRepository problemRepository;
    private final SandboxService sandboxService;

    @Override
    public TestGroup createTestGroup(TestGroup testGroup) {
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        testGroup.setCreateTime(now);
        testGroup.setUpdateTime(now);
        
        // 初始化测试用例列表
        if (testGroup.getTestCases() == null) {
            testGroup.setTestCases(new ArrayList<>());
        }
        
        // 为测试用例分配ID
        if (testGroup.getTestCases() != null && !testGroup.getTestCases().isEmpty()) {
            long testCaseId = 1;
            for (TestCase testCase : testGroup.getTestCases()) {
                testCase.setId(testCaseId++);
            }
        }
        
        return testGroupRepository.save(testGroup);
    }
    
  @Override
  public TestGroup createTestGroupWithStandardCode(StandardCodeRequest request) {
      // 从请求中提取必要信息
      TestGroup testGroup = request.getTestGroup();
      String standardCode = request.getStandardCode();
      String language = request.getLanguage();

      // 参数校验
      if (testGroup == null) {
          throw new IllegalArgumentException("测试集信息不能为空");
      }
      if (standardCode == null || standardCode.isEmpty()) {
          throw new IllegalArgumentException("标准代码不能为空");
      }
      if (language == null || language.isEmpty()) {
          throw new IllegalArgumentException("编程语言不能为空");
      }

      // 初始化测试集基本信息
      LocalDateTime now = LocalDateTime.now();
      testGroup.setCreateTime(now);
      testGroup.setUpdateTime(now);

      // 确保测试用例集合已初始化
      if (testGroup.getTestCases() == null) {
          testGroup.setTestCases(new ArrayList<>());
      }

      // 编译标准代码
      CompileResult compileResult = sandboxService.compileCode(CompileRequest.builder()
              .code(standardCode)
              .language(language)
              .build());

      // 编译失败处理
      if (compileResult == null || !compileResult.isSuccess()) {
          String errorMsg = compileResult != null ? compileResult.getErrorMessage() : "未知编译错误";
          log.error("标准代码编译失败: {}", errorMsg);
          throw new IllegalArgumentException("标准代码编译失败: " + errorMsg);
      }

      String fileId = compileResult.getFileId();

      // 如果有测试用例，则为每个用例运行标准代码生成期望输出
      if (testGroup.getTestCases() != null && !testGroup.getTestCases().isEmpty()) {
          try {
              // 使用虚拟线程并行执行所有测试用例，提高效率
              List<Thread> threads = new ArrayList<>();
              for (TestCase testCase : testGroup.getTestCases()) {
                  // 确保每个测试用例都有唯一ID
                  testCase.setId(testCase.getId() != null ? testCase.getId() : (long) threads.size() + 1);

                  if (testCase.getInput() != null && !testCase.getInput().isEmpty()) {
                      // 使用Java 21虚拟线程特性，提高并发性能
                      Thread virtualThread = Thread.ofVirtual()
                              .name("test-case-" + testCase.getId())
                              .start(() -> processTestCase(testCase, standardCode, language, fileId));
                      threads.add(virtualThread);
                  }
              }

              // 等待所有虚拟线程执行完毕
              for (Thread thread : threads) {
                  try {
                      thread.join();
                  } catch (InterruptedException e) {
                      log.error("等待测试用例处理线程被中断", e);
                      Thread.currentThread().interrupt();
                  }
              }
          } finally {
              // 资源清理：确保编译生成的文件被删除，防止资源泄漏
              if (fileId != null) {
                  sandboxService.deleteFile(fileId);
              }
          }
      }

      // 持久化测试集
      return testGroupRepository.save(testGroup);
  }

  /**
   * 处理单个测试用例，运行标准代码生成期望输出
   *
   * @param testCase 测试用例
   * @param standardCode 标准代码
   * @param language 编程语言
   * @param fileId 编译生成的文件ID
   */
  private void processTestCase(TestCase testCase, String standardCode, String language, String fileId) {
      try {
          RunResult runResult = sandboxService.runCode(RunRequest.builder()
                  .code(standardCode)
                  .language(language)
                  .input(testCase.getInput())
                  .fileId(fileId)
                  .build());

          // 检查执行结果
          if ("Accepted".equals(runResult.getStatus()) || runResult.getExitStatus() == 0) {
              // 运行成功，使用标准代码的输出作为期望输出
              testCase.setExpectedOutput(runResult.getStdout());
              log.info("测试用例 {} 使用标准代码生成期望输出: {}", testCase.getId(), runResult.getStdout());
          } else {
              // 运行失败，记录错误信息
              log.warn("测试用例 {} 标准代码执行失败: {}", testCase.getId(), runResult.getStatus());
              if (runResult.getStderr() != null && !runResult.getStderr().isEmpty()) {
                  log.warn("错误信息: {}", runResult.getStderr());
              }
          }
      } catch (Exception e) {
          log.error("测试用例 {} 执行标准代码生成期望输出时发生错误", testCase.getId(), e);
      }
  }


 @Override
 public TestGroup updateTestGroupWithStandardCode(String testGroupId, StandardCodeRequest request) {
     // 获取现有测试集
     TestGroup existingTestGroup = testGroupRepository.findById(testGroupId)
             .orElseThrow(() -> new IllegalArgumentException("测试集不存在: " + testGroupId));

     // 从请求中提取必要信息
     TestGroup newTestGroup = request.getTestGroup();
     String standardCode = request.getStandardCode();
     String language = request.getLanguage();

     // 参数校验
     if (standardCode == null || standardCode.isEmpty()) {
         throw new IllegalArgumentException("标准代码不能为空");
     }
     if (language == null || language.isEmpty()) {
         throw new IllegalArgumentException("编程语言不能为空");
     }

     // 更新基本信息
     if (newTestGroup != null) {
         if (StringUtils.hasText(newTestGroup.getName())) {
             existingTestGroup.setName(newTestGroup.getName());
         }
         if (StringUtils.hasText(newTestGroup.getDescription())) {
             existingTestGroup.setDescription(newTestGroup.getDescription());
         }

         // 更新测试用例（如果提供了新的测试用例列表）
         if (newTestGroup.getTestCases() != null) {
             // 为新的测试用例分配ID
             long maxId = 0;
             if (existingTestGroup.getTestCases() != null && !existingTestGroup.getTestCases().isEmpty()) {
                 maxId = existingTestGroup.getTestCases().stream()
                         .mapToLong(TestCase::getId)
                         .max()
                         .orElse(0);
             }

             for (TestCase testCase : newTestGroup.getTestCases()) {
                 if (testCase.getId() == null) {
                     testCase.setId(++maxId);
                 }
             }

             existingTestGroup.setTestCases(newTestGroup.getTestCases());
         }
     }

     // 编译标准代码
     CompileResult compileResult = sandboxService.compileCode(CompileRequest.builder()
             .code(standardCode)
             .language(language)
             .build());

     // 编译失败处理
     if (compileResult == null || !compileResult.isSuccess()) {
         String errorMsg = compileResult != null ? compileResult.getErrorMessage() : "未知编译错误";
         log.error("标准代码编译失败: {}", errorMsg);
         throw new IllegalArgumentException("标准代码编译失败: " + errorMsg);
     }

     String fileId = compileResult.getFileId();

     // 如果有测试用例，则为每个用例运行标准代码生成期望输出
     if (existingTestGroup.getTestCases() != null && !existingTestGroup.getTestCases().isEmpty()) {
         try {
             // 使用虚拟线程并行处理所有测试用例
             List<Thread> threads = new ArrayList<>();
             for (TestCase testCase : existingTestGroup.getTestCases()) {
                 if (testCase.getInput() != null && !testCase.getInput().isEmpty()) {
                     // 使用Java 21虚拟线程特性，提高并发性能
                     Thread virtualThread = Thread.ofVirtual()
                             .name("update-test-case-" + testCase.getId())
                             .start(() -> processTestCase(testCase, standardCode, language, fileId));
                     threads.add(virtualThread);
                 }
             }

             // 等待所有虚拟线程执行完毕
             for (Thread thread : threads) {
                 try {
                     thread.join();
                 } catch (InterruptedException e) {
                     log.error("等待测试用例处理线程被中断", e);
                     Thread.currentThread().interrupt();
                 }
             }
         } finally {
             // 资源清理：确保编译生成的文件被删除，防止资源泄漏
             if (fileId != null) {
                 sandboxService.deleteFile(fileId);
             }
         }
     }

     // 更新时间
     existingTestGroup.setUpdateTime(LocalDateTime.now());

     // 持久化并返回更新后的测试集
     return testGroupRepository.save(existingTestGroup);
 }



    @Override
    public TestGroup updateTestGroup(String testGroupId, TestGroup testGroup) {
        TestGroup existingTestGroup = testGroupRepository.findById(testGroupId)
                .orElseThrow(() -> new IllegalArgumentException("测试集不存在: " + testGroupId));

        // 更新基本信息
        if (testGroup != null) {
            if (StringUtils.hasText(testGroup.getName())) {
                existingTestGroup.setName(testGroup.getName());
            }
            if (StringUtils.hasText(testGroup.getDescription())) {
                existingTestGroup.setDescription(testGroup.getDescription());
            }

            // 更新测试用例（如果提供了新的测试用例列表）
            if (testGroup.getTestCases() != null) {
                // 为新的测试用例分配ID
                long maxId = 0;
                if (existingTestGroup.getTestCases() != null && !existingTestGroup.getTestCases().isEmpty()) {
                    maxId = existingTestGroup.getTestCases().stream()
                            .mapToLong(TestCase::getId)
                            .max()
                            .orElse(0);
                }

                for (TestCase testCase : testGroup.getTestCases()) {
                    if (testCase.getId() == null) {
                        testCase.setId(++maxId);
                    }
                }

                existingTestGroup.setTestCases(testGroup.getTestCases());
            }
        }

        // 更新时间
        existingTestGroup.setUpdateTime(LocalDateTime.now());

        return testGroupRepository.save(existingTestGroup);
    }



    @Override
    public boolean deleteTestGroup(String testGroupId) {
        TestGroup testGroup = testGroupRepository.findById(testGroupId)
            .orElseThrow(() -> new IllegalArgumentException("测试集不存在: " + testGroupId));

        testGroupRepository.delete(testGroup);
        return true;
    }

    @Override
    public TestGroup getTestGroupById(String testGroupId) {
        return testGroupRepository.findById(testGroupId)
            .orElseThrow(() -> new IllegalArgumentException("测试集不存在: " + testGroupId));
    }





    
    @Override
    public TestGroup updateTestCase(String testGroupId, Long testCaseId, TestCase testCase) {
        TestGroup testGroup = testGroupRepository.findById(testGroupId)
            .orElseThrow(() -> new IllegalArgumentException("测试集不存在: " + testGroupId));
        
        if (testGroup.getTestCases() == null || testGroup.getTestCases().isEmpty()) {
            throw new IllegalArgumentException("测试集中没有测试用例");
        }
        
        // 查找并更新测试用例
        boolean found = false;
        for (int i = 0; i < testGroup.getTestCases().size(); i++) {
            if (testGroup.getTestCases().get(i).getId().equals(testCaseId)) {
                // 保留原ID
                testCase.setId(testCaseId);
                testGroup.getTestCases().set(i, testCase);
                found = true;
                break;
            }
        }
        
        if (!found) {
            throw new IllegalArgumentException("测试用例不存在: " + testCaseId);
        }
        
        // 更新时间
        testGroup.setUpdateTime(LocalDateTime.now());
        
        return testGroupRepository.save(testGroup);
    }

    @Override
    public TestGroup addTestCase(String testGroupId, TestCase testCase) {
        TestGroup testGroup = testGroupRepository.findById(testGroupId)
            .orElseThrow(() -> new IllegalArgumentException("测试集不存在: " + testGroupId));
        
        // 初始化测试用例列表（如果为空）
        if (testGroup.getTestCases() == null) {
            testGroup.setTestCases(new ArrayList<>());
        }
        
        // 为新测试用例分配ID
        long maxId = 0;
        if (!testGroup.getTestCases().isEmpty()) {
            maxId = testGroup.getTestCases().stream()
                .mapToLong(TestCase::getId)
                .max()
                .orElse(0);
        }
        testCase.setId(maxId + 1);
        
        // 添加测试用例
        testGroup.getTestCases().add(testCase);
        
        // 更新时间
        testGroup.setUpdateTime(LocalDateTime.now());
        
        return testGroupRepository.save(testGroup);
    }

    @Override
    public TestGroup deleteTestCase(String testGroupId, Long testCaseId) {
        TestGroup testGroup = testGroupRepository.findById(testGroupId)
            .orElseThrow(() -> new IllegalArgumentException("测试集不存在: " + testGroupId));
        
        if (testGroup.getTestCases() == null || testGroup.getTestCases().isEmpty()) {
            throw new IllegalArgumentException("测试集中没有测试用例");
        }
        
        // 查找并删除测试用例
        boolean removed = testGroup.getTestCases().removeIf(tc -> tc.getId().equals(testCaseId));
        
        if (!removed) {
            throw new IllegalArgumentException("测试用例不存在: " + testCaseId);
        }
        
        // 更新时间
        testGroup.setUpdateTime(LocalDateTime.now());
        
        return testGroupRepository.save(testGroup);
    }
    
   @Override
        public String exportTestGroupJson(String testGroupId) {
            TestGroup testGroup = getTestGroupById(testGroupId);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                // 配置日期时间格式
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                // 格式化输出，方便阅读
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                return objectMapper.writeValueAsString(testGroup);
            } catch (JsonProcessingException e) {
                this.log.error("导出测试集JSON失败", e);
                throw new RuntimeException("导出测试集JSON失败: " + e.getMessage());
            }
        }

    @Override
    public List<TestGroup> getTestGroupsById(String testGroupId) {
        Optional<TestGroup> byId = testGroupRepository.findById(testGroupId);
        TestGroup testGroup = byId.get();
        return List.of(testGroup);
    }
}