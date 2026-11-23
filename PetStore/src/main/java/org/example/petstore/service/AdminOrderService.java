package org.example.petstore.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.petstore.entity.Account;
import org.example.petstore.entity.Item;
import org.example.petstore.entity.LineItem;
import org.example.petstore.entity.Orders;
import org.example.petstore.mapper.AccountMapper;
import org.example.petstore.mapper.ItemMapper;
import org.example.petstore.mapper.LineItemMapper;
import org.example.petstore.mapper.OrderStatusMapper;
import org.example.petstore.mapper.OrdersMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 后台订单管理服务（T-20261118-24）。
 *
 * <p>业务规则（需求裁定 R4）：订单**只能改状态**，明细不可改。</p>
 *
 * <p>发货扣库存是本卡的核心硬逻辑：</p>
 * <ol>
 *   <li><b>先全量校验</b>：遍历订单全部明细，逐个比对 SKU 库存；</li>
 *   <li>任一不足 → <b>直接抛异常整体拒绝</b>（此时尚未扣减任何库存，天然无部分扣减）；</li>
 *   <li>全部充足 → 统一扣减，扣到 0 的 SKU 状态置 {@code OUTOFSTOCK}；</li>
 * </ol>
 * 整个过程 {@code @Transactional}，异常回滚。
 */
@Service
public class AdminOrderService {

    /** 状态 id，来自 data.sql 的 order_status 种子数据（契约 §5.4） */
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_COMPLETED = 2;
    public static final int STATUS_SHIPPED = 3;
    public static final int STATUS_CANCELLED = 4;

    private static final String ITEM_OUT_OF_STOCK = "OUTOFSTOCK";

    private final OrdersMapper ordersMapper;
    private final LineItemMapper lineItemMapper;
    private final ItemMapper itemMapper;
    private final OrderStatusMapper orderStatusMapper;
    private final AccountMapper accountMapper;

    public AdminOrderService(OrdersMapper ordersMapper, LineItemMapper lineItemMapper, ItemMapper itemMapper,
                             OrderStatusMapper orderStatusMapper, AccountMapper accountMapper) {
        this.ordersMapper = ordersMapper;
        this.lineItemMapper = lineItemMapper;
        this.itemMapper = itemMapper;
        this.orderStatusMapper = orderStatusMapper;
        this.accountMapper = accountMapper;
    }

    /** 全部订单，按 id 降序 */
    public List<Orders> listOrders() {
        return ordersMapper.selectList(Wrappers.<Orders>lambdaQuery().orderByDesc(Orders::getId));
    }

    /** 状态 id → 中文名，供模板下标取值 */
    public Map<Integer, String> statusNames() {
        return orderStatusMapper.selectList(Wrappers.emptyWrapper()).stream()
                .collect(Collectors.toMap(s -> s.getId(), s -> s.getName(), (a, b) -> a));
    }

    /** 用户 id → 用户名，供列表显示下单人 */
    public Map<Integer, String> usernames() {
        return accountMapper.selectList(Wrappers.emptyWrapper()).stream()
                .collect(Collectors.toMap(Account::getId, Account::getUsername, (a, b) -> a));
    }

    /**
     * 发货：状态改为"已发货"并扣减库存。
     *
     * @return 成功提示文案
     * @throws IllegalArgumentException 订单不存在、状态不允许、或库存不足
     */
    @Transactional(rollbackFor = Exception.class)
    public String ship(Integer orderId) {
        Orders order = requireOrder(orderId);

        if (order.getStatusId() != null && order.getStatusId() == STATUS_CANCELLED) {
            throw new IllegalArgumentException("已取消订单不可发货");
        }
        if (order.getStatusId() != null && order.getStatusId() == STATUS_SHIPPED) {
            throw new IllegalArgumentException("该订单已发货，请勿重复发货");
        }

        List<LineItem> lines = lineItemMapper.selectList(
                Wrappers.<LineItem>lambdaQuery().eq(LineItem::getOrderId, orderId));
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("订单无明细，无法发货");
        }

        // 第一步：全量校验（此阶段绝不改任何数据）
        List<String> shortages = new ArrayList<>();
        for (LineItem line : lines) {
            Item item = itemMapper.selectById(line.getItemId());
            if (item == null) {
                shortages.add("SKU(id=" + line.getItemId() + ") 不存在");
                continue;
            }
            int need = line.getQuantity() == null ? 0 : line.getQuantity();
            int stock = item.getQuantity() == null ? 0 : item.getQuantity();
            if (stock < need) {
                String label = (item.getName() == null) ? ("SKU " + item.getItemid()) : item.getName();
                shortages.add(label + " 需 " + need + " 件，库存仅 " + stock + " 件");
            }
        }
        if (!shortages.isEmpty()) {
            // 整体拒绝：订单状态不变、库存一件未扣
            throw new IllegalArgumentException("库存不足：" + String.join("；", shortages));
        }

        // 第二步：全部充足，统一扣减
        for (LineItem line : lines) {
            Item item = itemMapper.selectById(line.getItemId());
            int stock = item.getQuantity() == null ? 0 : item.getQuantity();
            int left = stock - line.getQuantity();

            Item patch = new Item();
            patch.setId(item.getId());
            patch.setQuantity(left);
            if (left == 0) {
                patch.setStatus(ITEM_OUT_OF_STOCK);
            }
            itemMapper.updateById(patch);
        }

        // 第三步：改订单状态
        Orders patch = new Orders();
        patch.setId(orderId);
        patch.setStatusId(STATUS_SHIPPED);
        ordersMapper.updateById(patch);

        return "已发货，库存已扣减";
    }

    /**
     * 取消订单：仅改状态，**不回补库存**（该订单尚未发货，本就没扣过）。
     *
     * @throws IllegalArgumentException 订单不存在或已发货
     */
    @Transactional(rollbackFor = Exception.class)
    public String cancel(Integer orderId) {
        Orders order = requireOrder(orderId);

        if (order.getStatusId() != null && order.getStatusId() == STATUS_SHIPPED) {
            throw new IllegalArgumentException("已发货订单不可取消");
        }
        if (order.getStatusId() != null && order.getStatusId() == STATUS_CANCELLED) {
            throw new IllegalArgumentException("该订单已取消");
        }

        Orders patch = new Orders();
        patch.setId(orderId);
        patch.setStatusId(STATUS_CANCELLED);
        ordersMapper.updateById(patch);
        return "已取消";
    }

    private Orders requireOrder(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("订单 id 不能为空");
        }
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在：" + orderId);
        }
        return order;
    }
}
