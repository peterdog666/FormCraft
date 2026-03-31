package com.formcraft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("form_schema")
public class FormSchema {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    /** 表单字段定义 JSON */
    private String schema;

    /**
     * 校验规则 JSON，格式：
     * {
     *   "fields": [
     *     {
     *       "id": "field_1",
     *       "rules": {
     *         "required": true,
     *         "minLength": 2,
     *         "maxLength": 50,
     *         "pattern": "email|phone|url|number|custom",
     *         "customPattern": "^[0-9]{11}$",
     *         "errorMsg": "请输入有效的手机号"
     *       }
     *     }
     *   ]
     * }
     */
    private String validationRules;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
