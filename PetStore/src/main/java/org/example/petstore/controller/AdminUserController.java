package org.example.petstore.controller;

import jakarta.servlet.http.HttpSession;
import org.example.petstore.entity.Account;
import org.example.petstore.service.AdminUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 后台用户管理（T-20261118-24）。契约 §5.2.3 / §5.4。
 *
 * <p><b>防锁死</b>：不允许停用自己。本系统不提供"取消管理员"入口，
 * 因而不存在把自己降权导致无人管理的路径。</p>
 */
@Controller
@RequestMapping("/admin")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /** 用户列表 */
    @GetMapping("/users")
    public String list(Model model, HttpSession session) {
        model.addAttribute("accounts", adminUserService.listUsers());
        model.addAttribute("currentUserId", currentUserId(session));
        return "admin/users";
    }

    /** 重置为固定初始密码，并把新口令一次性回显 */
    @PostMapping("/user/{id}/reset-password")
    public String resetPassword(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            String newPassword = adminUserService.resetPassword(id);
            ra.addFlashAttribute("msg", "密码已重置为：" + newPassword + "，请通知该用户重新登录");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    /** 启用/停用切换（禁止停用自己） */
    @PostMapping("/user/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Integer id, HttpSession session, RedirectAttributes ra) {
        try {
            ra.addFlashAttribute("msg", adminUserService.toggleStatus(id, currentUserId(session)));
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("err", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    /** 从 session 取当前管理员 id；取不到时返回 -1（不会等于任何真实用户 id） */
    private Integer currentUserId(HttpSession session) {
        Object loginUser = (session == null) ? null : session.getAttribute(AccountController.SESSION_LOGIN_USER);
        if (loginUser instanceof Account account && account.getId() != null) {
            return account.getId();
        }
        return -1;
    }
}
