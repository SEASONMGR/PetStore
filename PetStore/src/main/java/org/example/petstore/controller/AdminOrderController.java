package org.example.petstore.controller;

import org.example.petstore.service.AdminOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 后台订单管理（T-20261118-24）。契约 §5.2.4 / §5.4。
 *
 * <p>订单**只能改状态**，明细不可改（需求裁定 R4）。发货会扣减库存，库存不足整体拒绝。</p>
 */
@Controller
@RequestMapping("/admin")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    /** 订单列表 */
    @GetMapping("/orders")
    public String list(Model model) {
        model.addAttribute("orders", adminOrderService.listOrders());
        model.addAttribute("statusNames", adminOrderService.statusNames());
        model.addAttribute("usernames", adminOrderService.usernames());
        return "admin/orders";
    }

    /** 发货：状态改已发货 + 扣库存（不足则整体拒绝） */
    @PostMapping("/order/{id}/ship")
    public String ship(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            ra.addFlashAttribute("msg", adminOrderService.ship(id));
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/admin/orders";
    }

    /** 取消订单：仅改状态（已发货不可取消） */
    @PostMapping("/order/{id}/cancel")
    public String cancel(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            ra.addFlashAttribute("msg", adminOrderService.cancel(id));
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/admin/orders";
    }
}
