package org.example.petstore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品（表 product）。imagePath 形如 /images/products/xxx.png，
 * 文件由 res-01 提供，落在 src/main/resources/static/images/products/ 下。
 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 业务编号，如 DOG-001 */
    private String productid;

    /** 所属分类 id */
    private Integer categoryId;

    private String name;

    private String description;

    /** 图片路径：/images/products/&lt;文件名&gt;.png */
    private String imagePath;

    /** 参考价；SKU 实际单价以 item 为准 */
    private BigDecimal listPrice;

    /** ON_SALE / OFF_SHELF */
    private String status;

    private Integer sortOrder;

    private LocalDateTime createdAt;
}
