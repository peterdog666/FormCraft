package com.formcraft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formcraft.entity.FormSubmission;
import com.formcraft.mapper.FormSubmissionMapper;
import com.formcraft.service.SubmissionService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class SubmissionServiceImpl extends ServiceImpl<FormSubmissionMapper, FormSubmission> implements SubmissionService {

    @Override
    public FormSubmission submit(Long formId, String data, String ip) {
        FormSubmission submission = new FormSubmission();
        submission.setFormId(formId);
        submission.setData(data);
        submission.setSubmitTime(LocalDateTime.now());
        submission.setIp(ip);
        save(submission);
        return submission;
    }

    @Override
    public Page<FormSubmission> getByFormId(Long formId, Integer page, Integer size) {
        Page<FormSubmission> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<FormSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormSubmission::getFormId, formId);
        wrapper.orderByDesc(FormSubmission::getSubmitTime);
        return page(pageParam, wrapper);
    }
}
