package sim.forum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateUserInfoDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 20, message = "用户名长度必须在1到20个字符之间")
    // 用户的昵称
    private String name;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "请输入正确的邮箱格式")
    // 用户的邮箱
    private String email;

    // 头像
    private String avatar;
}
