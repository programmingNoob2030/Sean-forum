package sim.forum.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordDTO extends CheckEmailCodeDTO{
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 14, message = "密码长度必须在8到14个字符之间")
    @Pattern(
            // 正则拆解：
            // ^(?=.*[0-9])(?=.*[a-zA-Z]) -> 必须同时包含数字和字母 (最常用的“2种”)
            // [^\s\u4e00-\u9fa5]+ -> 全程不允许空格和中文
            // $ -> 结束
            regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])[^\\s\\u4e00-\u9fa5]{8,14}$",
            message = "密码必须包含字母和数字，且不能有空格或中文"
    )
    private String password;
}


