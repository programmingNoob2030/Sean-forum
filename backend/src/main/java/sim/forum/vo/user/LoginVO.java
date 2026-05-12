package sim.forum.vo.user;

import lombok.Data;

import java.util.Date;

@Data
public class LoginVO {
    // 用户的昵称
    private String name;

    // 用户的标识
    private String token;

    // 用户的头像
    private String avatar;

    // 用户的邮箱
    private String email;
    // 用户的注册时间
    private Date registerTime;

    // 用户的上次登录时间
    private Date lastLoginTime;

    // 用户的发帖数
    private Integer postCount;
}
