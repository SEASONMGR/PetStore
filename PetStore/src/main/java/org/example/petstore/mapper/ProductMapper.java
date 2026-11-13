package org.example.petstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.petstore.entity.Product;

/**
 * 商品 Mapper。按分类查商品、按 id 查单个商品均可用 BaseMapper 的条件构造器完成。
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
