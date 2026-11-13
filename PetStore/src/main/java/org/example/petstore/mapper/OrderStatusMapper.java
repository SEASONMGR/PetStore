package org.example.petstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.petstore.entity.OrderStatus;

/**
 * 订单状态字典 Mapper（本任务暂无 Controller 使用，供后续订单模块调用）。
 */
@Mapper
public interface OrderStatusMapper extends BaseMapper<OrderStatus> {
}
