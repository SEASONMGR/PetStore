package org.example.petstore.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.petstore.controller.AccountController;
import org.example.petstore.entity.Account;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 后台管理权限拦截器（T-20261118-24）。
 *
 * <p>只拦 {@code /admin/**}，不影响 {@code /catalog}、{@code /product}、{@code /account/**}。</p>
 *
 * <p>三态处置（契约 §5.1）：</p>
 * <ol>
 *   <li>未登录 → {@code 302 redirect:/account/login}</li>
 *   <li>已登录但非管理员 → <b>抛出 {@link AdminForbiddenException}</b>，由
 *       {@code AdminAccessExceptionHandler} 渲染 {@code error/403} 并置 HTTP 403</li>
 *   <li>管理员 → 放行</li>
 * </ol>
 *
 * <p><b>安全默认</b>：session 里 {@code loginUser} 为 null、或 {@code admin} 为 null / false，
 * 一律按非管理员处理。不使用 Spring Security（需求裁定 R1）。</p>
 */
public class AdminInterceptor implements HandlerInterceptor {

    private static final String LOGIN_PATH = "/account/login";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        Object loginUser = (session == null) ? null : session.getAttribute(AccountController.SESSION_LOGIN_USER);

        // 1) 未登录 → 重定向登录页（302）
        if (!(loginUser instanceof Account account)) {
            response.sendRedirect(request.getContextPath() + LOGIN_PATH);
            return false;
        }

        // 2) 已登录但非管理员 → 中断处理链，交全局异常处理器渲染 403
        if (!Boolean.TRUE.equals(account.getAdmin())) {
            throw new AdminForbiddenException(request.getRequestURI());
        }

        // 3) 管理员 → 放行
        return true;
    }

    /** 非管理员访问后台时抛出，携带请求路径供 403 视图展示 */
    public static class AdminForbiddenException extends RuntimeException {

        private final String path;

        public AdminForbiddenException(String path) {
            super("该页面仅管理员可访问");
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        /** 便于日志定位：抛出点即拦截器 */
        public int status() {
            return HttpStatus.FORBIDDEN.value();
        }
    }
}
