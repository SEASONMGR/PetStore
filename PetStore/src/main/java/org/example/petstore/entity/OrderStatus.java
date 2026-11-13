package org.example.petstore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 订单状态字典（表 order_status），如 P=待付款、C=已完成。
 */
@Data
@TableName("order_status")
public class OrderStatus {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 状态码：P / C / D / X */
    private String code;

    private String name;

    private String description;

    private Integer sortOrder;
}
