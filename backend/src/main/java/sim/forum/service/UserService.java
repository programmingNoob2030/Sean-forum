package sim.forum.service;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import sim.forum.dto.user.*;
import sim.forum.entity.User;
import sim.forum.vo.user.LoginVO;

@Validated
public interface UserService {

    LoginVO login(@Valid LoginDTO dto);

    User register(@Valid RegisterDTO dto);

    Boolean isEmailValid(@Valid VerifyEmailDTO dto);

    Boolean isEmailCodeValid(@Valid CheckEmailCodeDTO dto);

    Boolean resetPassword(@Valid ResetPasswordDTO dto);

    String updateUserAvatar(MultipartFile file, Long userId);

    LoginVO updateUserInfo(@Valid UpdateUserInfoDTO dto, Long userId);

    User selectUserById(Long userId);


}
