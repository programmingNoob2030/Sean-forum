package sim.forum;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class ForumApplicationTests {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Test
    void contextLoads() {
    }

    @Test
    void testBasicOpers() {
        // 1. 存入一个字符串
        redisTemplate.opsForValue().set("forum:test:user", "SeanLi");

        // 2. 取出来看看对不对
        String value = redisTemplate.opsForValue().get("forum:test:user");
        System.out.println("从Redis拿到的值是: " + value);

        // 【实验环节】：执行完这行代码，你立刻去 redis-cli 里输入 get forum:test:user
        // 看看是不是能拿到 "SeanLi"？
    }
}
