package com.formcraft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formcraft.entity.FormSchema;
import com.formcraft.entity.FormSubmission;
import com.formcraft.mapper.FormSchemaMapper;
import com.formcraft.mapper.FormSubmissionMapper;
import com.formcraft.service.FormService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FormServiceImpl extends ServiceImpl<FormSchemaMapper, FormSchema> implements FormService {

    private final FormSubmissionMapper submissionMapper;

    public FormServiceImpl(FormSubmissionMapper submissionMapper) {
        this.submissionMapper = submissionMapper;
    }

    @Override
    public FormSchema create(FormSchema form) {
        form.setStatus(0);
        form.setCreateTime(LocalDateTime.now());
        form.setUpdateTime(LocalDateTime.now());
        save(form);
        return form;
    }

    @Override
    public FormSchema update(FormSchema form) {
        form.setUpdateTime(LocalDateTime.now());
        updateById(form);
        return form;
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }

    @Override
    public FormSchema getById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<FormSchema> list() {
        LambdaQueryWrapper<FormSchema> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(FormSchema::getCreateTime);
        return list(wrapper);
    }

    @Override
    public void publish(Long id) {
        FormSchema form = baseMapper.selectById(id);
        if (form != null) {
            form.setStatus(1);
            form.setUpdateTime(LocalDateTime.now());
            updateById(form);
        }
    }

    @Override
    public void unpublish(Long id) {
        FormSchema form = baseMapper.selectById(id);
        if (form != null) {
            form.setStatus(0);
            form.setUpdateTime(LocalDateTime.now());
            updateById(form);
        }
    }

    @Override
    public Long getSubmissionCount(Long formId) {
        LambdaQueryWrapper<FormSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormSubmission::getFormId, formId);
        return submissionMapper.selectCount(wrapper);
    }
}
