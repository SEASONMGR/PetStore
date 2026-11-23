package org.example.petstore.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.petstore.entity.Category;
import org.example.petstore.entity.Item;
import org.example.petstore.entity.Product;
import org.example.petstore.mapper.CategoryMapper;
import org.example.petstore.mapper.ItemMapper;
import org.example.petstore.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 后台商品管理服务（T-20261118-24）。
 *
 * <p>商品删除走**逻辑删除**（需求裁定 R5）：{@link Product} 上标了 {@code @TableLogic}，
 * MyBatis Plus 会自动把查询补上 {@code deleted = 0}、把 delete 改写为 UPDATE。因此本类
 * **不需要**手写 deleted 过滤条件，也不做任何物理删除。</p>
 *
 * <p>分页为**内存分页**：未引入 {@code mybatis-plus-jsqlparser}（分页插件所在模块，
 * 不在现有依赖里，任务卡禁止新增依赖），故查询全量后在 Java 侧切页。演示数据规模下足够。</p>
 */
@Service
public class AdminProductService {

    /** 每页条数（契约 §5.3 固定 10，不开放前端设置） */
    public static final int PAGE_SIZE = 10;

    private static final String STATUS_ON_SALE = "ON_SALE";
    private static final String STATUS_OFF_SHELF = "OFF_SHELF";

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final ItemMapper itemMapper;

    public AdminProductService(ProductMapper productMapper, CategoryMapper categoryMapper, ItemMapper itemMapper) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.itemMapper = itemMapper;
    }

    /** 全部分类，按 sortOrder、id 升序（供下拉与筛选） */
    public List<Category> listCategories() {
        return categoryMapper.selectList(
                Wrappers.<Category>lambdaQuery().orderByAsc(Category::getSortOrder).orderByAsc(Category::getId));
    }

    /** 分类 id → 分类名，供模板直接下标取值 */
    public Map<Integer, String> categoryNames() {
        return listCategories().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
    }

    /**
     * 商品列表查询（含关键字与分类筛选，内存分页）。
     *
     * @param keyword    商品名/编号模糊搜索，可空
     * @param categoryId 分类筛选，可空
     * @param page       页码，从 1 开始；为 null 或越界会被纠正
     */
    public ProductPage query(String keyword, Integer categoryId, Integer page) {
        List<Product> all = productMapper.selectList(Wrappers.<Product>lambdaQuery()
                .orderByAsc(Product::getSortOrder)
                .orderByAsc(Product::getId));

        String kw = (keyword == null) ? "" : keyword.trim().toLowerCase();
        List<Product> filtered = all.stream()
                .filter(p -> kw.isEmpty()
                        || (p.getName() != null && p.getName().toLowerCase().contains(kw))
                        || (p.getProductid() != null && p.getProductid().toLowerCase().contains(kw)))
                .filter(p -> categoryId == null || categoryId.equals(p.getCategoryId()))
                .toList();

        int totalPages = Math.max(1, (filtered.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int current = (page == null || page < 1) ? 1 : Math.min(page, totalPages);
        int from = (current - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filtered.size());
        List<Product> pageItems = (from >= filtered.size())
                ? Collections.emptyList()
                : filtered.subList(from, to);

        return new ProductPage(List.copyOf(pageItems), current, totalPages);
    }

    /** 按 id 查商品（逻辑删除的查不到） */
    public Optional<Product> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(productMapper.selectById(id));
    }

    /** 某商品的 SKU 列表，按 id 升序 */
    public List<Item> listItems(Integer productId) {
        return itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                .eq(Item::getProductId, productId)
                .orderByAsc(Item::getId));
    }

    /**
     * 新建或更新商品。
     *
     * @return 保存后的商品
     * @throws IllegalArgumentException 必填项缺失或业务编号重复
     */
    public Product save(Product form) {
        if (form == null) {
            throw new IllegalArgumentException("商品数据不能为空");
        }
        if (form.getName() == null || form.getName().isBlank()) {
            throw new IllegalArgumentException("商品名称不能为空");
        }
        if (form.getProductid() == null || form.getProductid().isBlank()) {
            throw new IllegalArgumentException("商品编号不能为空");
        }
        if (form.getCategoryId() == null) {
            throw new IllegalArgumentException("请选择所属分类");
        }
        if (form.getListPrice() == null || form.getListPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("参考价不能为空且不能为负");
        }

        String productid = form.getProductid().trim();
        // 业务编号唯一性：排除自身
        Long dup = productMapper.selectCount(Wrappers.<Product>lambdaQuery()
                .eq(Product::getProductid, productid)
                .ne(form.getId() != null, Product::getId, form.getId()));
        if (dup != null && dup > 0) {
            throw new IllegalArgumentException("商品编号已存在：" + productid);
        }

        Product toSave = new Product();
        toSave.setId(form.getId());
        toSave.setProductid(productid);
        toSave.setCategoryId(form.getCategoryId());
        toSave.setName(form.getName().trim());
        toSave.setDescription(form.getDescription());
        toSave.setImagePath(form.getImagePath());
        toSave.setListPrice(form.getListPrice());
        toSave.setSortOrder(form.getSortOrder() == null ? 0 : form.getSortOrder());

        if (form.getId() == null) {
            // 新建默认上架（契约 §5.4）
            toSave.setStatus(STATUS_ON_SALE);
            productMapper.insert(toSave);
        } else {
            Product existing = productMapper.selectById(form.getId());
            if (existing == null) {
                throw new IllegalArgumentException("商品不存在或已删除：" + form.getId());
            }
            // 编辑时不覆盖状态（上下架由 toggle 专属负责）
            toSave.setStatus(existing.getStatus());
            productMapper.updateById(toSave);
        }
        return toSave;
    }

    /**
     * 上架/下架切换。
     *
     * @return 切换后的状态文案（"已上架" / "已下架"）
     */
    public String toggleStatus(Integer id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在或已删除：" + id);
        }
        boolean onSale = STATUS_ON_SALE.equals(product.getStatus());
        Product patch = new Product();
        patch.setId(id);
        patch.setStatus(onSale ? STATUS_OFF_SHELF : STATUS_ON_SALE);
        productMapper.updateById(patch);
        return onSale ? "已下架" : "已上架";
    }

    /** 逻辑删除（@TableLogic 自动改写为 UPDATE deleted=1，不物理删除） */
    public void delete(Integer id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在或已删除：" + id);
        }
        productMapper.deleteById(id);
    }

    /**
     * 保存/更新某商品的 SKU（库存与价格，卖家最常用）。
     *
     * @throws IllegalArgumentException 商品或 SKU 不存在、价格/库存非法
     */
    public void saveItem(Integer productId, Item form) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在或已删除：" + productId);
        }
        if (form == null || form.getId() == null) {
            throw new IllegalArgumentException("SKU id 不能为空");
        }
        if (form.getListPrice() == null || form.getListPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("单价不能为空且不能为负");
        }
        if (form.getQuantity() == null || form.getQuantity() < 0) {
            throw new IllegalArgumentException("库存不能为空且不能为负");
        }

        Item existing = itemMapper.selectById(form.getId());
        if (existing == null || !productId.equals(existing.getProductId())) {
            throw new IllegalArgumentException("SKU 不存在或不属于该商品");
        }

        Item patch = new Item();
        patch.setId(form.getId());
        patch.setName(form.getName());
        patch.setListPrice(form.getListPrice());
        patch.setQuantity(form.getQuantity());
        // 库存归零自动置缺货，恢复则置有货（DISCONTINUED 不动）
        if ("DISCONTINUED".equals(existing.getStatus())) {
            patch.setStatus(existing.getStatus());
        } else {
            patch.setStatus(form.getQuantity() > 0 ? "INSTOCK" : "OUTOFSTOCK");
        }
        itemMapper.updateById(patch);
    }

    /** 分页结果（契约 §5.2.1 的 products/page/totalPages 来源） */
    public record ProductPage(List<Product> products, Integer page, Integer totalPages) {
    }
}
