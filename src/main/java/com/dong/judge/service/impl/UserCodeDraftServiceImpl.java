package com.dong.judge.service.impl;

import com.dong.judge.dao.repository.UserCodeDraftRepository;
import com.dong.judge.model.pojo.judge.UserCodeDraft;
import com.dong.judge.service.UserCodeDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserCodeDraftServiceImpl implements UserCodeDraftService {
    
    private final UserCodeDraftRepository userCodeDraftRepository;
    
    @Override
    public UserCodeDraft saveDraft(String userId, String problemId, String code, String language) {
        UserCodeDraft draft = userCodeDraftRepository.findByUserIdAndProblemId(userId, problemId)
                .orElse(UserCodeDraft.builder()
                        .userId(userId)
                        .problemId(problemId)
                        .build());
        
        draft.setCode(code);
        draft.setLanguage(language);
        draft.setUpdatedAt(LocalDateTime.now());
        
        return userCodeDraftRepository.save(draft);
    }
    
    @Override
    public UserCodeDraft getDraft(String userId, String problemId) {
        return userCodeDraftRepository.findByUserIdAndProblemId(userId, problemId)
                .orElse(null);
    }
    
    @Override
    public void deleteDraft(String userId, String problemId) {
        userCodeDraftRepository.deleteByUserIdAndProblemId(userId, problemId);
    }
} 