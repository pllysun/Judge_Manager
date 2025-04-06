package com.dong.judge.service;

import com.dong.judge.model.pojo.judge.UserCodeDraft;

public interface UserCodeDraftService {
    UserCodeDraft saveDraft(String userId, String problemId, String code, String language);
    UserCodeDraft getDraft(String userId, String problemId);
    void deleteDraft(String userId, String problemId);
} 