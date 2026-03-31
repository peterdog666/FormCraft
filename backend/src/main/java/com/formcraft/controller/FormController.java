package com.formcraft.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formcraft.common.R;
import com.formcraft.entity.FormSchema;
import com.formcraft.entity.FormSubmission;
import com.formcraft.service.FormService;
import com.formcraft.service.SubmissionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/form")
public class FormController {

    private final FormService formService;
    private final SubmissionService submissionService;

    public FormController(FormService formService, SubmissionService submissionService) {
        this.formService = formService;
        this.submissionService = submissionService;
    }

    @PostMapping
    public R<FormSchema> create(@RequestBody FormSchema form) {
        FormSchema created = formService.create(form);
        return R.success(created);
    }

    @PutMapping("/{id}")
    public R<FormSchema> update(@PathVariable Long id, @RequestBody FormSchema form) {
        form.setId(id);
        FormSchema updated = formService.update(form);
        return R.success(updated);
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        formService.delete(id);
        return R.success();
    }

    @GetMapping("/{id}")
    public R<FormSchema> getById(@PathVariable Long id) {
        FormSchema form = formService.getById(id);
        return R.success(form);
    }

    @GetMapping("/list")
    public R<List<Map<String, Object>>> list() {
        List<FormSchema> forms = formService.list();
        List<Map<String, Object>> result = forms.stream().map(form -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", form.getId());
            map.put("name", form.getName());
            map.put("description", form.getDescription());
            map.put("schema", form.getSchema());
            map.put("status", form.getStatus());
            map.put("createTime", form.getCreateTime());
            map.put("updateTime", form.getUpdateTime());
            map.put("submissionCount", formService.getSubmissionCount(form.getId()));
            return map;
        }).toList();
        return R.success(result);
    }

    @PutMapping("/{id}/publish")
    public R<Void> publish(@PathVariable Long id) {
        formService.publish(id);
        return R.success();
    }

    @PutMapping("/{id}/unpublish")
    public R<Void> unpublish(@PathVariable Long id) {
        formService.unpublish(id);
        return R.success();
    }

    @PostMapping("/submission/{formId}")
    public R<Void> submit(@PathVariable Long formId, @RequestBody Map<String, Object> body, @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor) {
        String data = body.get("data") != null ? body.get("data").toString() : "{}";
        String ip = forwardedFor != null ? forwardedFor : "127.0.0.1";
        submissionService.submit(formId, data, ip);
        return R.success();
    }

    @GetMapping("/submission/{formId}")
    public R<Page<FormSubmission>> getSubmissions(@PathVariable Long formId, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        Page<FormSubmission> result = submissionService.getByFormId(formId, page, size);
        return R.success(result);
    }
}
