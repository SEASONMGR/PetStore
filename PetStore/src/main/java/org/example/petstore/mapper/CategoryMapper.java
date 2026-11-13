package org.example.petstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.petstore.entity.Category;

/**
 * 分类 Mapper。GET /catalog 通过 selectList(Wrappers...) 按 sortOrder 排序。
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
