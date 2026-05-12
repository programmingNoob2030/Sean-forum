package sim.forum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyEmailDTO {
    // 用户注册用的邮箱
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "请输入正确的邮箱格式")
    private String email;
}
