package com.formcraft.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formcraft.common.R;
import com.formcraft.entity.FormSubmission;
import com.formcraft.service.SubmissionService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/submission")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/{formId}")
    public R<Void> submit(@PathVariable Long formId, @RequestBody Map<String, Object> body, @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor) {
        String data = body.get("data") != null ? body.get("data").toString() : "{}";
        String ip = forwardedFor != null ? forwardedFor : "127.0.0.1";
        submissionService.submit(formId, data, ip);
        return R.success();
    }

    @GetMapping("/{formId}")
    public R<Page<FormSubmission>> getByFormId(@PathVariable Long formId, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        Page<FormSubmission> result = submissionService.getByFormId(formId, page, size);
        return R.success(result);
    }
}
