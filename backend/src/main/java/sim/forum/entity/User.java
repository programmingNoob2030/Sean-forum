package sim.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName("users")
@Data
public class User {
    @TableId(type = IdType.AUTO)
    /* 用户的ID */
    private Long id;

    /* 用户的昵称 */
    private String name;

    /* 用户的头像 */
    private String avatar;

    /* 用户的邮箱 */
    private String email;

    /* 用户的密码 */
    private String password;

    /* 用户的注册时间 */
    private Date registerTime;

    /* 用户的上次登录时间 */
    private Date lastLoginTime;

}
