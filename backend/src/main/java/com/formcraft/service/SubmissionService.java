package com.formcraft.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formcraft.entity.FormSubmission;

import java.time.LocalDateTime;
import java.util.List;

public interface SubmissionService {

    FormSubmission submit(Long formId, String data, String ip);

    Page<FormSubmission> getByFormId(Long formId, Integer page, Integer size);

    /**
     * 按日期范围查询提交记录（用于 Excel 导出）
     *
     * @param formId  表单 ID
     * @param startDt 开始时间（含），null 表示不限
     * @param endDt   结束时间（含），null 表示不限
     */
    List<FormSubmission> getByFormIdAndDateRange(Long formId, LocalDateTime startDt, LocalDateTime endDt);
}
