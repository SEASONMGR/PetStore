package org.example.petstore.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.petstore.entity.Category;
import org.example.petstore.entity.Product;
import org.example.petstore.service.CatalogService;
import org.springframework.http.HttpStatus;
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
 *   <li>分类/商品不存在 → 视图名 {@code error/404}，并显式设置 HTTP 404 状态码</li>
 * </ul>
 *
 * <p>契约见 {@code 项目/docs/接口契约.md}，改动请同步更新契约（公约 §13.3）。</p>
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
    public String listProductsByCategory(@PathVariable("categoryId") Integer categoryId,
                                         Model model,
                                         HttpServletResponse response) {
        Optional<Category> category = catalogService.findCategory(categoryId);
        if (category.isEmpty()) {
            return notFound(response, model, "分类不存在：" + categoryId);
        }
        model.addAttribute("category", category.get());
        model.addAttribute("products", catalogService.listProductsByCategory(categoryId));
        return "category";
    }

    /** 商品详情页：列出该商品的 SKU */
    @GetMapping("/product/{productId}")
    public String viewProduct(@PathVariable("productId") Integer productId,
                              Model model,
                              HttpServletResponse response) {
        Optional<Product> product = catalogService.findProduct(productId);
        if (product.isEmpty()) {
            return notFound(response, model, "商品不存在：" + productId);
        }
        model.addAttribute("product", product.get());
        model.addAttribute("items", catalogService.listItemsByProduct(productId));
        return "product";
    }

    /**
     * 统一走 404：返回 error/404 视图，同时把响应状态码置为 404。
     *
     * <p>注意：仅 {@code return "error/404"} 是不够的——视图名只决定渲染哪个模板，
     * 响应码仍是 200，浏览器/爬虫会当成"页面存在"。故此处必须 setStatus。
     * 同时补上模板 error/404.html 期待的标准属性（status / path / message / error），
     * 该模板原本按 BasicErrorController 的属性编写，不经它渲染时需自行提供。</p>
     */
    private String notFound(HttpServletResponse response, Model model, String message) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        model.addAttribute("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        model.addAttribute("message", message);
        return VIEW_NOT_FOUND;
    }
}
