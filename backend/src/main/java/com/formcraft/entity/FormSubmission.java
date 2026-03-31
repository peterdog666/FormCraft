package com.formcraft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("form_submission")
public class FormSubmission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long formId;

    private String data;

    private LocalDateTime submitTime;

    private String ip;
}
