package org.example.petstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.petstore.entity.Item;

/**
 * SKU Mapper。GET /product/{productId} 通过 selectList 按 productId 查规格列表。
 */
@Mapper
public interface ItemMapper extends BaseMapper<Item> {
}
