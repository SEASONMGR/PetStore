package org.example.petstore.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.petstore.entity.Category;
import org.example.petstore.entity.Item;
import org.example.petstore.entity.Product;
import org.example.petstore.mapper.CategoryMapper;
import org.example.petstore.mapper.ItemMapper;
import org.example.petstore.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 商品目录查询服务（只读）。
 * Controller 只负责挑视图与塞 model，查询细节放这里，避免在 Controller 里堆条件构造器。
 */
@Service
public class CatalogService {

    /** 上架状态，与 schema.sql 中 product.status 的取值一致 */
    private static final String STATUS_ON_SALE = "ON_SALE";

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final ItemMapper itemMapper;

    public CatalogService(CategoryMapper categoryMapper, ProductMapper productMapper, ItemMapper itemMapper) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.itemMapper = itemMapper;
    }

    /** 所有分类，按 sortOrder、id 升序 */
    public List<Category> listCategories() {
        return categoryMapper.selectList(
                Wrappers.<Category>lambdaQuery().orderByAsc(Category::getSortOrder).orderByAsc(Category::getId));
    }

    /** 按 id 查分类，查不到返回空 */
    public Optional<Category> findCategory(Integer categoryId) {
        if (categoryId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(categoryMapper.selectById(categoryId));
    }

    /** 某分类下所有上架商品，按 sortOrder、id 升序 */
    public List<Product> listProductsByCategory(Integer categoryId) {
        return productMapper.selectList(Wrappers.<Product>lambdaQuery()
                .eq(Product::getCategoryId, categoryId)
                .eq(Product::getStatus, STATUS_ON_SALE)
                .orderByAsc(Product::getSortOrder)
                .orderByAsc(Product::getId));
    }

    /** 按 id 查商品，查不到返回空 */
    public Optional<Product> findProduct(Integer productId) {
        if (productId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(productMapper.selectById(productId));
    }

    /** 某商品下的所有 SKU，按 id 升序（暂不按 status 过滤，缺货也展示并标注） */
    public List<Item> listItemsByProduct(Integer productId) {
        return itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                .eq(Item::getProductId, productId)
                .orderByAsc(Item::getId));
    }
}
