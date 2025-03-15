package com.dong.judge.model.pojo.judge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document(collection = "submissions")
@NoArgsConstructor
@AllArgsConstructor
public class Submission {
    @Id
    private String id;
    private String userId;
    private String problemId;
    private String code;
    private String language;
    private String status;
    private int passedCount;
    private int totalCount;
    private long executionTime;
    private long memoryUsed;
    private String compileError;
    private LocalDateTime submissionTime;
}
