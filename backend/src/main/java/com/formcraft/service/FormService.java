package com.formcraft.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formcraft.entity.FormSchema;
import java.util.List;

public interface FormService {

    FormSchema create(FormSchema form);

    FormSchema update(FormSchema form);

    void delete(Long id);

    FormSchema getById(Long id);

    List<FormSchema> list();

    void publish(Long id);

    void unpublish(Long id);

    Long getSubmissionCount(Long formId);
}
