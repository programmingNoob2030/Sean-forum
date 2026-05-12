package sim.forum.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;

public class JWTUtils {
    private static final String SECRET = "SICHUAN_CS_PRO_2026"; // 签名密钥
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000; // 24小时过期

    /**
     * 登录成功后调用：生成Token
     */
    public static String createToken(Long userId) {
        return JWT.create()
                .withClaim("userId", userId)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .sign(Algorithm.HMAC256(SECRET));
    }

    /**
     * 拦截器调用：解析Token
     */
    public static Long parseToken(String token) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(SECRET)).build().verify(token);

            var claim = jwt.getClaim("userId");
            // 如果这个 Claim 是空的，或者没拿到值
            if (claim.isNull()) {
                return null;

            }
            return claim.as(Long.class);

        } catch (Exception e) {
            // 既然现在出问题了，建议这里打印一下堆栈，看看是不是过期了或者签名真没对上
            e.printStackTrace();
            return null;
        }
    }
}