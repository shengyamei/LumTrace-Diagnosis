package org.csu.lumtrace.lumtracediagnosis.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.csu.lumtrace.lumtracediagnosis.common.util.JwtUtil;
import org.csu.lumtrace.lumtracediagnosis.common.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 鉴权拦截器
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${lumtrace.jwt.header:Authorization}")
    private String header;

    @Value("${lumtrace.jwt.prefix:Bearer }")
    private String prefix;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String auth = request.getHeader(header);
        if (auth == null || !auth.startsWith(prefix)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token缺失\"}");
            return false;
        }
        try {
            String token = auth.substring(prefix.length()).trim();
            var claims = jwtUtil.parse(token);
            Long userId = Long.valueOf(claims.get("userId").toString());
            String role = String.valueOf(claims.get("role"));
            UserContext.set(userId, role);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
