package org.example.petstore.interceptor;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * 把 {@link AdminInterceptor.AdminForbiddenException} 转为 {@code error/403} 视图 + HTTP 403。
 *
 * <p>之所以用异常而非拦截器里直接 forward：Spring MVC 中没有"按视图名 forward"的官方途径，
 * 而异常 → {@code ModelAndView} 是标准链路，能保证：</p>
 * <ul>
 *   <li>控制器方法<b>不会</b>被执行（拦截器在 preHandle 抛，处理链立即中断）；</li>
 *   <li>响应状态码是真正的 <b>403</b>，不是 200（与 T-11 修 404 同一教训）；</li>
 *   <li>视图由 Thymeleaf 正常解析，model 属性齐备。</li>
 * </ul>
 */
@ControllerAdvice
public class AdminAccessExceptionHandler {

    /** 403 视图名，模板约定为 templates/error/403.html（dev-02 提供） */
    public static final String VIEW_FORBIDDEN = "error/403";

    @ExceptionHandler(AdminInterceptor.AdminForbiddenException.class)
    public ModelAndView handleForbidden(AdminInterceptor.AdminForbiddenException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        ModelAndView mav = new ModelAndView(VIEW_FORBIDDEN);
        mav.addObject("status", HttpStatus.FORBIDDEN.value());
        mav.addObject("error", HttpStatus.FORBIDDEN.getReasonPhrase());
        mav.addObject("message", ex.getMessage());
        mav.addObject("path", ex.getPath());
        return mav;
    }
}
