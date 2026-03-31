package com.formcraft.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formcraft.entity.FormSubmission;

public interface SubmissionService {

    FormSubmission submit(Long formId, String data, String ip);

    Page<FormSubmission> getByFormId(Long formId, Integer page, Integer size);
}
