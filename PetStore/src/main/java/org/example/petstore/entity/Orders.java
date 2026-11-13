package org.example.petstore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 订单（表 orders）。orderid 为业务单号，accountId/statusId 为外键。
 */
@Data
@TableName("orders")
public class Orders {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 业务单号，从 1000 起 */
    private Integer orderid;

    /** 下单用户 id */
    private Integer accountId;

    /** 订单状态 id（order_status.id） */
    private Integer statusId;

    private LocalDate orderDate;

    private String shipAddress;

    private String shipCity;

    private String shipState;

    private String shipZip;

    private String shipCountry;

    /** 订单总额（由明细汇总） */
    private BigDecimal totalPrice;
}
