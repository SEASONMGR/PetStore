package org.example.petstore.config;

import org.example.petstore.interceptor.AdminInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置（T-20261118-24）：注册后台管理权限拦截器。
 *
 * <p>只拦截 {@code /admin/**}；{@code /catalog}、{@code /product}、{@code /account/**}
 * 以及静态资源均不受影响（契约 §5.1，已实测）。</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminInterceptor())
                .addPathPatterns("/admin/**");
    }
}
