package org.example.petstore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品 SKU（表 item）：同一商品的不同规格，带单价与库存。
 */
@Data
@TableName("item")
public class Item {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** SKU 编号，如 DOG-001-01 */
    private String itemid;

    /** 所属商品 id */
    private Integer productId;

    /** 规格名，如 5kg 装 */
    private String name;

    private String description;

    /** 单价 */
    private BigDecimal listPrice;

    /** 成本价 */
    private BigDecimal unitCost;

    /** 库存 */
    private Integer quantity;

    /** INSTOCK / OUTOFSTOCK / DISCONTINUED */
    private String status;

    /** 图片路径：/images/products/&lt;文件名&gt;.png */
    private String imagePath;
}
