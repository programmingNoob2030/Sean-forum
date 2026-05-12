package sim.forum.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckEmailCodeDTO extends VerifyEmailDTO{
    // 用户输入的邮箱验证码
    @NotBlank(message = "验证码不能为空")
    private String code;

}
