package org.example.petstore.controller;

import org.example.petstore.entity.Category;
import org.example.petstore.entity.Product;
import org.example.petstore.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

/**
 * 商品展示：分类页与商品详情页。
 *
 * <ul>
 *   <li>GET /catalog/{categoryId} → 视图名 {@code category}，model：{@code category}、{@code products}</li>
 *   <li>GET /product/{productId}  → 视图名 {@code product}，model：{@code product}、{@code items}</li>
 *   <li>分类/商品不存在 → 视图名 {@code error/404}（模板由 dev-02 实现）</li>
 * </ul>
 */
@Controller
public class ProductController {

    /** 404 视图名，模板约定为 templates/error/404.html */
    private static final String VIEW_NOT_FOUND = "error/404";

    private final CatalogService catalogService;

    public ProductController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /** 分类页：列出该分类下的商品 */
    @GetMapping("/catalog/{categoryId}")
    public String listProductsByCategory(@PathVariable("categoryId") Integer categoryId, Model model) {
        Optional<Category> category = catalogService.findCategory(categoryId);
        if (category.isEmpty()) {
            model.addAttribute("message", "分类不存在：" + categoryId);
            return VIEW_NOT_FOUND;
        }
        model.addAttribute("category", category.get());
        model.addAttribute("products", catalogService.listProductsByCategory(categoryId));
        return "category";
    }

    /** 商品详情页：列出该商品的 SKU */
    @GetMapping("/product/{productId}")
    public String viewProduct(@PathVariable("productId") Integer productId, Model model) {
        Optional<Product> product = catalogService.findProduct(productId);
        if (product.isEmpty()) {
            model.addAttribute("message", "商品不存在：" + productId);
            return VIEW_NOT_FOUND;
        }
        model.addAttribute("product", product.get());
        model.addAttribute("items", catalogService.listItemsByProduct(productId));
        return "product";
    }
}
