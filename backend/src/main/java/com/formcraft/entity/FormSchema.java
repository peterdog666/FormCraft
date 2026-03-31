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

    private String schema;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
