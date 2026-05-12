package sim.forum.dto.user;

import lombok.Data;

@Data
public class LoginDTO {


    // 用户的昵称
    private String name;

    // 用户的密码
    private String password;
}
