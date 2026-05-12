package sim.forum.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sim.forum.context.UserContext;
import sim.forum.dto.user.*;
import sim.forum.result.Result;
import sim.forum.entity.User;
import sim.forum.service.FileUploadService;
import sim.forum.service.UserService;
import sim.forum.vo.user.LoginVO;

import lombok.extern.slf4j.Slf4j; // 建议加上日志记录

@Slf4j
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private FileUploadService fileUploadService; // 注入文件上传服务
    @PostMapping("/users")
    public Result<User> register(@Valid @RequestBody RegisterDTO dto){
        User user = userService.register(dto);
        return Result.success(user);
    }

    @PostMapping("/session")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto){
        LoginVO vo = userService.login(dto);
        return Result.success(vo);
    }
    @GetMapping("/email")
    public Result<Boolean> verifyEmail(@Valid VerifyEmailDTO dto){
        Boolean isValid =  userService.isEmailValid(dto);
        return Result.success(isValid);
    }

    @GetMapping("/code")
    public Result<Boolean> verifyCode(@Valid CheckEmailCodeDTO dto){
        Boolean isValid =  userService.isEmailCodeValid(dto);
        return Result.success(isValid);
    }

    @PutMapping("/password")
    public Result<Boolean> resetPassword(@Valid @RequestBody ResetPasswordDTO dto){
        Boolean isValid = userService.resetPassword(dto);
        return Result.success(isValid);
    }

    @PostMapping("/user/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        // 1. 从 ThreadLocal/BaseContext 获取当前登录用户 ID
        // 假设你的拦截器已经把解析好的 JWT ID 放进去了
        Long userId = UserContext.getUserId();
        // 2. 一行代码解决战斗
        String avatarPath = userService.updateUserAvatar(file, userId);

        // 3. 返回成功结果
        return Result.success(avatarPath);
    }

    @PutMapping("/info")
    public Result<LoginVO> updateUserInfo(@RequestBody UpdateUserInfoDTO dto){
        Long userId = UserContext.getUserId();
        LoginVO vo = userService.updateUserInfo(dto, userId);
        return Result.success(vo);
    }

}
