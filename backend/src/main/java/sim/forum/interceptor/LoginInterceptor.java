package sim.forum.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod; // 记得导入这个，用来识别方法
import org.springframework.web.servlet.HandlerInterceptor;
import sim.forum.annotation.OptionalAuth; // 导入你刚刚写的注解
import sim.forum.context.UserContext;
import sim.forum.utils.JWTUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // --- 1. 新增逻辑：看看这个接口有没有贴 @OptionalAuth 标签 ---
        boolean isOptional = false;
        if (handler instanceof HandlerMethod hm) {
            // 如果方法上有这个注解，isOptional 就是 true
            isOptional = hm.hasMethodAnnotation(OptionalAuth.class);
        }

        // --- 2. 你的原有逻辑：尝试拿 Token ---
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                Long userId = JWTUtils.parseToken(token);
                if (userId != null) {
                    UserContext.setUserId(userId);
                    return true; // 只要 Token 是对的，管他有没有标签，直接放行
                }
            } catch (Exception e) {
                // 如果 Token 坏了（过期或乱填），且这接口【不是】免检的，就报错
                if (!isOptional) {
                    response.setStatus(401);
                    return false;
                }
                // 如果是免检接口，即便 Token 坏了也无所谓，后面会处理
            }
        }

        // --- 3. 核心判定：没 Token 怎么办？ ---
        if (isOptional) {
            // 是免检接口（比如帖子列表），没证件也放行
            // 此时 UserContext 里的 userId 是 null，完美匹配你的 SQL 逻辑
            return true;
        }

        // 既没有证件，又不是免检接口，保安把你拦下
        response.setStatus(401);
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 4. 请求结束，一定要清理背包
        UserContext.removeUserId();
    }
}