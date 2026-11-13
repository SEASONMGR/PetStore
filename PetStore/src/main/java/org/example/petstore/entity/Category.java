package org.example.petstore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 分类（表 category），后台管理按 sortOrder 排序展示。
 */
@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 业务编号，如 DOGS */
    private String catid;

    private String name;

    private String description;

    private Integer sortOrder;
}
