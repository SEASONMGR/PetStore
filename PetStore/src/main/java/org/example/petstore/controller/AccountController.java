package org.example.petstore.controller;

import jakarta.servlet.http.HttpSession;
import org.example.petstore.entity.Account;
import org.example.petstore.service.AccountService;
import org.example.petstore.service.CaptchaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * 用户模块：登录、注册、退出。
 *
 * <p><b>实验用明文口令</b>（需求裁定 R1），生产须改哈希并接入 Spring Security，详见 {@link AccountService}。</p>
 *
 * <p>视图与 model 契约见 {@code 项目/docs/接口契约.md}；模板由 dev-02 提供：
 * {@code account/login}、{@code account/register}。</p>
 */
@Controller
@RequestMapping("/account")
public class AccountController {

    /** 登录态在 HttpSession 中的键名（契约固定值，dev-02 的模板如需读取请用同名字符串） */
    public static final String SESSION_LOGIN_USER = "loginUser";

    /** 验证码失败的固定文案（契约 §2.5 约定，前端如需断言请按此字符串匹配） */
    private static final String CAPTCHA_ERROR = "验证码错误";

    private final AccountService accountService;

    private final CaptchaService captchaService;

    public AccountController(AccountService accountService, CaptchaService captchaService) {
        this.accountService = accountService;
        this.captchaService = captchaService;
    }

    /** 登录页 */
    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        // 已登录则直接回目录，避免重复登录
        if (session.getAttribute(SESSION_LOGIN_USER) != null) {
            return "redirect:/catalog";
        }
        // 每次渲染都换新题（覆盖 session 中的旧答案）
        model.addAttribute("captchaQuestion", captchaService.generate(session));
        return "account/login";
    }

    /**
     * 登录提交：成功 → 重定向 /catalog；失败 → 回登录页并带 error 消息。
     * 口令错误与用户不存在返回同一句提示，避免暴露账号是否存在。
     *
     * <p>顺序：先校验验证码（一次性，验完即失效），通过后才查账号。</p>
     */
    @PostMapping("/login")
    public String login(@RequestParam(value = "username", required = false) String username,
                        @RequestParam(value = "password", required = false) String password,
                        @RequestParam(value = "captcha", required = false) String captcha,
                        Model model,
                        HttpSession session) {
        if (!captchaService.verify(session, captcha)) {
            return loginBackWithError(model, session, username, CAPTCHA_ERROR);
        }
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return loginBackWithError(model, session, username, "请输入用户名与密码");
        }
        Optional<Account> account = accountService.login(username, password);
        if (account.isEmpty()) {
            return loginBackWithError(model, session, username, "用户名或密码错误");
        }
        // 只放会话必要字段：写入前清掉口令，避免会话中留存明文或经模板意外输出
        Account loginUser = account.get();
        loginUser.setPassword(null);
        session.setAttribute(SESSION_LOGIN_USER, loginUser);
        return "redirect:/catalog";
    }

    /** 回登录页并带错误提示；同时重新生成验证码题（旧答案已在 verify 中失效） */
    private String loginBackWithError(Model model, HttpSession session, String username, String error) {
        model.addAttribute("error", error);
        model.addAttribute("username", username);
        model.addAttribute("captchaQuestion", captchaService.generate(session));
        return "account/login";
    }

    /** 注册页 */
    @GetMapping("/register")
    public String registerPage(Model model, HttpSession session) {
        // 供模板 th:object 使用的空对象
        model.addAttribute("account", new Account());
        model.addAttribute("captchaQuestion", captchaService.generate(session));
        return "account/register";
    }

    /**
     * 注册提交：成功 → 重定向登录页；失败（用户名/邮箱重复）→ 回注册页带 error 与回填内容。
     */
    @PostMapping("/register")
    public String register(@RequestParam(value = "username", required = false) String username,
                           @RequestParam(value = "password", required = false) String password,
                           @RequestParam(value = "email", required = false) String email,
                           @RequestParam(value = "firstName", required = false) String firstName,
                           @RequestParam(value = "lastName", required = false) String lastName,
                           @RequestParam(value = "phone", required = false) String phone,
                           @RequestParam(value = "address", required = false) String address,
                           @RequestParam(value = "city", required = false) String city,
                           @RequestParam(value = "state", required = false) String state,
                           @RequestParam(value = "zip", required = false) String zip,
                           @RequestParam(value = "country", required = false) String country,
                           @RequestParam(value = "captcha", required = false) String captcha,
                           Model model,
                           HttpSession session) {
        Account form = new Account();
        form.setUsername(username);
        form.setEmail(email);
        form.setFirstName(firstName);
        form.setLastName(lastName);
        form.setPhone(phone);
        form.setAddress(address);
        form.setCity(city);
        form.setState(state);
        form.setZip(zip);
        form.setCountry(country);

        // 先校验证码（一次性，验完即失效），再校验必填与重复
        if (!captchaService.verify(session, captcha)) {
            return registerBackWithError(model, session, form, CAPTCHA_ERROR);
        }
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password) || !StringUtils.hasText(email)) {
            return registerBackWithError(model, session, form, "用户名、密码、邮箱均为必填项");
        }

        try {
            Account toSave = new Account();
            toSave.setUsername(username);
            toSave.setPassword(password);
            toSave.setEmail(email);
            toSave.setFirstName(firstName);
            toSave.setLastName(lastName);
            toSave.setPhone(phone);
            toSave.setAddress(address);
            toSave.setCity(city);
            toSave.setState(state);
            toSave.setZip(zip);
            toSave.setCountry(country);
            accountService.register(toSave);
        } catch (IllegalArgumentException ex) {
            return registerBackWithError(model, session, form, ex.getMessage());
        }
        // 注册成功：跳登录页（带一次性提示参数）
        return "redirect:/account/login?registered=true";
    }

    /** 回注册页并带错误提示；同时重新生成验证码题（旧答案已在 verify 中失效） */
    private String registerBackWithError(Model model, HttpSession session, Account form, String error) {
        model.addAttribute("error", error);
        model.addAttribute("account", form);
        model.addAttribute("captchaQuestion", captchaService.generate(session));
        return "account/register";
    }

    /** 退出：清空会话（解绑登录态）后回目录 */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(SESSION_LOGIN_USER);
        session.invalidate();
        return "redirect:/catalog";
    }
}
