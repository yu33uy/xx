package com.github.w4o.xx.manage.common.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * WebMvc配置
 *
 * @author Frank
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NotNull ResourceHandlerRegistry registry) {

    }

    @Override
    public void extendMessageConverters(@NotNull List<HttpMessageConverter<?>> converters) {

    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 支持跨域
        //添加映射路径
        registry.addMapping("/**")
                //放行哪些原始域
                .allowedOriginPatterns(CorsConfiguration.ALL)
                //是否发送Cookie信息
                .allowCredentials(false)
                //放行哪些原始域(请求方式)
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                //放行哪些原始域(头部信息)
                .allowedHeaders("*");
    }

}
