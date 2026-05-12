// 路径: src/test/java/sim/forum/service/UserServiceTest.java

package sim.forum.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import sim.forum.dto.user.LoginDTO;
import sim.forum.dto.user.RegisterDTO;
import sim.forum.entity.User;
import sim.forum.vo.user.LoginVO;

@SpringBootTest // 如果需要用到数据库或依赖注入，带上它
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testRegister() {
        // 测试注册逻辑
        // userService.register(...);
        RegisterDTO register = new RegisterDTO();
        // register.setName("123456789101112131415132545646"); // 用户名长度必须在1到20个字符之间
        register.setName("aStudFromSasu");

        // register.setPassword("testPassword"); // 密码必须包含数字和字母(8-14位)，不能有数字和中文
        register.setPassword("123sasu321");

        // register.setEmail("testEmail"); // 错误的邮箱格式
        register.setEmail("123@sasu.com");
        // 是否执行操作
        User user = userService.register(register);
        Assertions.assertNotNull(user);

        // 是否创建成功
        Long id = user.getId();
        Assertions.assertNotNull(id);
    }

    @Test
    void testLogin() {
        // 测试登录逻辑
        // userService.login(...);
        LoginDTO dto = new LoginDTO();
//         dto.setName("testName"); // 必须使用数据库中存在的用户名
        dto.setName("aStudFromSasu");
//         dto.setPassword("testPassword"); // 必须使用用户名对应的密码
        dto.setPassword("123sasu321");
        LoginVO vo = userService.login(dto);
        // 是否执行成功、是否查询成功
        Assertions.assertNotNull(vo);
    }
}