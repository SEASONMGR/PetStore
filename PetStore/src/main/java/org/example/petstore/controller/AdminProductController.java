package org.example.petstore.controller;

import org.example.petstore.entity.Item;
import org.example.petstore.entity.Product;
import org.example.petstore.service.AdminProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 后台商品管理（T-20261118-24）。契约 §5.2.1 / §5.2.2 / §5.4。
 *
 * <p>所有写操作均为 POST；成功后 redirect 到列表页带 {@code msg}，失败带 {@code err}。
 * 权限由 {@code AdminInterceptor} 统一兜住，本类不再重复判断。</p>
 */
@Controller
@RequestMapping("/admin")
public class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    /** 商品列表（分页 + 关键字 + 分类筛选） */
    @GetMapping("/products")
    public String list(@RequestParam(value = "page", required = false) Integer page,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "categoryId", required = false) Integer categoryId,
                       Model model) {
        AdminProductService.ProductPage result = adminProductService.query(keyword, categoryId, page);
        model.addAttribute("products", result.products());
        model.addAttribute("page", result.page());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("categories", adminProductService.listCategories());
        model.addAttribute("categoryNames", adminProductService.categoryNames());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        return "admin/products";
    }

    /** 新建商品表单 */
    @GetMapping("/product/new")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", adminProductService.listCategories());
        model.addAttribute("items", List.of());
        model.addAttribute("isNew", Boolean.TRUE);
        return "admin/product-form";
    }

    /** 编辑商品表单（含 SKU 行内编辑区） */
    @GetMapping("/product/{id}/edit")
    public String editForm(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
        Optional<Product> product = adminProductService.findById(id);
        if (product.isEmpty()) {
            ra.addFlashAttribute("err", "商品不存在或已删除");
            return "redirect:/admin/products";
        }
        model.addAttribute("product", product.get());
        model.addAttribute("categories", adminProductService.listCategories());
        model.addAttribute("items", adminProductService.listItems(id));
        model.addAttribute("isNew", Boolean.FALSE);
        return "admin/product-form";
    }

    /** 保存商品（id 为空=新建） */
    @PostMapping("/product/save")
    public String save(@RequestParam(value = "id", required = false) Integer id,
                       @RequestParam(value = "productid", required = false) String productid,
                       @RequestParam(value = "categoryId", required = false) Integer categoryId,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "description", required = false) String description,
                       @RequestParam(value = "imagePath", required = false) String imagePath,
                       @RequestParam(value = "listPrice", required = false) BigDecimal listPrice,
                       @RequestParam(value = "sortOrder", required = false) Integer sortOrder,
                       Model model, RedirectAttributes ra) {
        Product form = new Product();
        form.setId(id);
        form.setProductid(productid);
        form.setCategoryId(categoryId);
        form.setName(name);
        form.setDescription(description);
        form.setImagePath(imagePath);
        form.setListPrice(listPrice);
        form.setSortOrder(sortOrder);
        try {
            Product saved = adminProductService.save(form);
            ra.addFlashAttribute("msg", id == null ? "商品已创建" : "商品已保存");
            // 新建后跳到编辑页，方便立刻维护 SKU
            return (id == null) ? "redirect:/admin/product/" + saved.getId() + "/edit"
                    : "redirect:/admin/products";
        } catch (IllegalArgumentException ex) {
            // 校验失败回表单（200），保留用户输入
            model.addAttribute("product", form);
            model.addAttribute("categories", adminProductService.listCategories());
            model.addAttribute("items", id == null ? List.of() : adminProductService.listItems(id));
            model.addAttribute("isNew", id == null);
            model.addAttribute("err", ex.getMessage());
            return "admin/product-form";
        }
    }

    /** 上架/下架切换 */
    @PostMapping("/product/{id}/toggle")
    public String toggle(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            ra.addFlashAttribute("msg", adminProductService.toggleStatus(id));
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/admin/products";
    }

    /** 逻辑删除 */
    @PostMapping("/product/{id}/delete")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            adminProductService.delete(id);
            ra.addFlashAttribute("msg", "已删除");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/admin/products";
    }

    /** 保存 SKU 的库存与价格（行内编辑） */
    @PostMapping("/product/{id}/item/save")
    public String saveItem(@PathVariable("id") Integer productId,
                           @RequestParam(value = "itemId", required = false) Integer itemId,
                           @RequestParam(value = "name", required = false) String name,
                           @RequestParam(value = "listPrice", required = false) BigDecimal listPrice,
                           @RequestParam(value = "quantity", required = false) Integer quantity,
                           RedirectAttributes ra) {
        Item form = new Item();
        form.setId(itemId);
        form.setName(name);
        form.setListPrice(listPrice);
        form.setQuantity(quantity);
        try {
            adminProductService.saveItem(productId, form);
            ra.addFlashAttribute("msg", "规格已保存");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/admin/product/" + productId + "/edit";
    }
}
