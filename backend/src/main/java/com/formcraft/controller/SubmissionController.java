package com.formcraft.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formcraft.common.R;
import com.formcraft.entity.FormSchema;
import com.formcraft.entity.FormSubmission;
import com.formcraft.service.FormService;
import com.formcraft.service.SubmissionService;
import com.formcraft.util.ValidationUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/submission")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final FormService formService;
    private final ObjectMapper objectMapper;

    public SubmissionController(SubmissionService submissionService,
                                FormService formService,
                                ObjectMapper objectMapper) {
        this.submissionService = submissionService;
        this.formService = formService;
        this.objectMapper = objectMapper;
    }

    /**
     * 提交表单数据（含服务端校验）
     */
    @PostMapping("/{formId}")
    public R<Void> submit(
            @PathVariable Long formId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor) {

        // 1. 获取表单定义（含校验规则）
        FormSchema form = formService.getById(formId);
        if (form == null) {
            return R.error(404, "表单不存在");
        }

        // 2. 解析提交数据
        String dataStr = body.get("data") != null ? body.get("data").toString() : "{}";
        Map<String, Object> dataMap;
        try {
            dataMap = objectMapper.readValue(dataStr, new TypeReference<>() {});
        } catch (Exception e) {
            return R.error(400, "数据格式错误");
        }

        // 3. 服务端校验
        Map<String, String> errors = ValidationUtil.validateFormData(
                form.getSchema(),
                form.getValidationRules(),
                dataMap
        );
        if (!errors.isEmpty()) {
            // 返回 400 + 第一条错误信息（也可返回全部）
            String firstError = errors.values().iterator().next();
            return R.error(400, firstError);
        }

        // 4. 保存提交记录
        String ip = forwardedFor != null ? forwardedFor : "127.0.0.1";
        submissionService.submit(formId, dataStr, ip);
        return R.success();
    }

    /**
     * 分页查询提交记录
     */
    @GetMapping("/{formId}")
    public R<Page<FormSubmission>> getByFormId(
            @PathVariable Long formId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<FormSubmission> result = submissionService.getByFormId(formId, page, size);
        return R.success(result);
    }

    /**
     * 导出 Excel
     * GET /api/submission/{formId}/export?startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping("/{formId}/export")
    public void exportExcel(
            @PathVariable Long formId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {

        // 1. 获取表单信息
        FormSchema form = formService.getById(formId);
        if (form == null) {
            response.sendError(HttpStatus.NOT_FOUND.value(), "表单不存在");
            return;
        }

        // 2. 解析字段定义
        List<Map<String, Object>> fields;
        try {
            fields = objectMapper.readValue(
                    form.getSchema() != null ? form.getSchema() : "[]",
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "解析表单结构失败");
            return;
        }

        // 3. 查询提交记录（带日期筛选）
        LocalDateTime startDt = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDt   = endDate   != null ? endDate.atTime(23, 59, 59) : null;
        List<FormSubmission> submissions = submissionService.getByFormIdAndDateRange(formId, startDt, endDt);

        // 4. 生成 Excel
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("提交记录");

            // ---- 样式 ----
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorder(headerStyle);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setWrapText(true);
            setBorder(dataStyle);

            CellStyle altDataStyle = workbook.createCellStyle();
            altDataStyle.setWrapText(true);
            altDataStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            altDataStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorder(altDataStyle);

            // ---- 第一行：表单名称（合并单元格）----
            int totalCols = fields.size() + 3; // 序号 + 提交时间 + IP + 字段列
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(form.getName());
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, totalCols - 1));

            // ---- 第二行：表头 ----
            Row headerRow = sheet.createRow(1);
            headerRow.setHeightInPoints(22);
            String[] fixedHeaders = {"序号", "提交时间", "IP地址"};
            for (int i = 0; i < fixedHeaders.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(fixedHeaders[i]);
                cell.setCellStyle(headerStyle);
            }
            for (int i = 0; i < fields.size(); i++) {
                Cell cell = headerRow.createCell(i + 3);
                Object label = fields.get(i).get("label");
                cell.setCellValue(label != null ? label.toString() : "字段" + (i + 1));
                cell.setCellStyle(headerStyle);
            }

            // ---- 数据行 ----
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int rowIdx = 0; rowIdx < submissions.size(); rowIdx++) {
                FormSubmission sub = submissions.get(rowIdx);
                Row dataRow = sheet.createRow(rowIdx + 2);
                CellStyle style = rowIdx % 2 == 0 ? dataStyle : altDataStyle;

                // 序号
                Cell seqCell = dataRow.createCell(0);
                seqCell.setCellValue(rowIdx + 1);
                seqCell.setCellStyle(style);

                // 提交时间
                Cell timeCell = dataRow.createCell(1);
                timeCell.setCellValue(sub.getSubmitTime() != null
                        ? sub.getSubmitTime().format(dtf) : "-");
                timeCell.setCellStyle(style);

                // IP
                Cell ipCell = dataRow.createCell(2);
                ipCell.setCellValue(sub.getIp() != null ? sub.getIp() : "-");
                ipCell.setCellStyle(style);

                // 字段数据
                Map<String, Object> dataMap;
                try {
                    dataMap = objectMapper.readValue(
                            sub.getData() != null ? sub.getData() : "{}",
                            new TypeReference<>() {}
                    );
                } catch (Exception e) {
                    dataMap = Map.of();
                }

                for (int colIdx = 0; colIdx < fields.size(); colIdx++) {
                    Object fieldId = fields.get(colIdx).get("id");
                    Cell dataCell = dataRow.createCell(colIdx + 3);
                    if (fieldId != null) {
                        Object val = dataMap.get(fieldId.toString());
                        if (val instanceof List<?> list) {
                            dataCell.setCellValue(String.join("; ", list.stream()
                                    .map(Object::toString).toList()));
                        } else {
                            dataCell.setCellValue(val != null ? val.toString() : "-");
                        }
                    } else {
                        dataCell.setCellValue("-");
                    }
                    dataCell.setCellStyle(style);
                }
            }

            // ---- 自动列宽 ----
            for (int i = 0; i < totalCols; i++) {
                sheet.autoSizeColumn(i);
                // 防止列宽过窄或过宽
                int width = sheet.getColumnWidth(i);
                if (width < 3000) sheet.setColumnWidth(i, 3000);
                if (width > 15000) sheet.setColumnWidth(i, 15000);
            }

            // 5. 写出响应
            String fileName = URLEncoder.encode(
                    form.getName() + "_提交记录_" + LocalDate.now() + ".xlsx",
                    StandardCharsets.UTF_8
            ).replace("+", "%20");

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + fileName);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        }
    }

    /** 设置单元格四周边框 */
    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
