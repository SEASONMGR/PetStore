package org.example.petstore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细（表 lineitem）。unitPrice 是下单时的成交单价快照。
 */
@Data
@TableName("lineitem")
public class LineItem {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 所属订单 id（orders.id） */
    private Integer orderId;

    /** SKU id（item.id） */
    private Integer itemId;

    private Integer quantity;

    /** 成交单价快照 */
    private BigDecimal unitPrice;
}
