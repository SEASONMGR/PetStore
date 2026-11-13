package org.example.petstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.example.petstore.service.CatalogService;

/**
 * 目录首页：GET /catalog
 * 视图名：catalog（src/main/resources/templates/catalog.html，由 dev-02 实现）
 * model 属性：
 *   - categories : List&lt;Category&gt; 全部分类（按 sortOrder 升序）
 */
@Controller
public class CategoryController {

    private final CatalogService catalogService;

    public CategoryController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/catalog")
    public String listCategories(Model model) {
        model.addAttribute("categories", catalogService.listCategories());
        return "catalog";
    }
}
