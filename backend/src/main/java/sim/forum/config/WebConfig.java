package sim.forum.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sim.forum.interceptor.LoginInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;
    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截所有
                .excludePathPatterns("/session", "/users",
                        "/code", "/email","/password","/uploads/**"); // 排除登录和注册
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Define the URL pattern (Virtual Path)
        // 2. Map it to the physical path (Absolute Path)
        // Note: The "file:" prefix is mandatory for absolute disk paths
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }

    @PostConstruct
    public void debug() {
        System.out.println("DEBUG: Loaded upload path is: " + uploadPath);
    }

}

