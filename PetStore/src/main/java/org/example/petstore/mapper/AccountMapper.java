package org.example.petstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.petstore.entity.Account;

/**
 * 用户 Mapper。通用 CRUD 由 BaseMapper 提供，自定义 SQL 用 @Select 注解或 resources/mapper/*.xml。
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
