package sim.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import sim.forum.dto.user.*;
import sim.forum.entity.User;
import sim.forum.event.post.PostCreateEvent;
import sim.forum.event.post.PostDeleteEvent;
import sim.forum.event.post.PostRestoreEvent;
import sim.forum.exception.BusinessException;
import sim.forum.mapper.UserMapper;
import sim.forum.service.CountService;
import sim.forum.service.FileUploadService;
import sim.forum.service.MailService;
import sim.forum.service.UserService;
import sim.forum.utils.JWTUtils;
import sim.forum.vo.user.LoginVO;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Validated
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailService mailService;
    @Autowired
    private FileUploadService fileUploadService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CountService countService;

    @Override
    public LoginVO login(@Valid LoginDTO dto) {
        // 查询条件
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<User>()
                .eq(User::getName, dto.getName());
        User user = userMapper.selectOne(query);
        if (user != null && (BCrypt.checkpw(dto.getPassword(),user.getPassword()))) {
            user.setLastLoginTime(new Date());
            userMapper.updateById(user);
            LoginVO loginVO =  userMapper.loginResult(dto);
            String token = JWTUtils.createToken(user.getId());
            loginVO.setToken(token);
            return loginVO;
        }
        else {throw new BusinessException("账号或密码错误，请检查您的输入!");}
    }

    @Override
    public User register(@Valid RegisterDTO dto) {
        User user = this.isNameRegistered(dto.getName());
        if (user != null) throw new BusinessException("用户名已经存在了，请换一个吧!");
        user = new User();
        BeanUtils.copyProperties(dto, user);
        // 加密, 生成密文
        String hashPassword = BCrypt.hashpw(dto.getPassword(),BCrypt.gensalt());
        user.setPassword(hashPassword);

        userMapper.insert(user);
        return userMapper.selectById(user.getId());
    }

    public User isEmailRegistered(String email){
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email);
        return userMapper.selectOne(query);

    }
    public User isNameRegistered(String name){
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<User>()
                .eq(User::getName, name);
        return userMapper.selectOne(query);
    }
    @Override
    public Boolean isEmailValid(@Valid VerifyEmailDTO dto) {
        User user = isEmailRegistered(dto.getEmail());
        if(user != null){
            // 2. 生成 6 位随机验证码
            // 如果没引入 Hutool，可以用 (int)((Math.random()*9+1)*100000)
            String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));

            // 3. 存入 Redis (Key 为 auth:code:邮箱)
            String redisKey = "auth:code:" + dto.getEmail();
            redisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);

            // 4. 发送邮件
            mailService.sendCodeMail(dto.getEmail(), code);
            return true;
        }else return false;
    }
    @Override
    public Boolean isEmailCodeValid(@Valid CheckEmailCodeDTO dto) {
        User user = isEmailRegistered(dto.getEmail());
        if (user != null){
            // 从redis中拿邮箱和验证码
            String code = redisTemplate.opsForValue().get("auth:code:"+dto.getEmail());
            return dto.getCode().equals(code);
        }else return false;
    }
    @Override
    public Boolean resetPassword(ResetPasswordDTO dto) {
        User user = isEmailRegistered(dto.getEmail());
        String code = redisTemplate.opsForValue().get("auth:code:"+ dto.getEmail());
        if (user != null && code.equals(dto.getCode())){
            String hashPassword = BCrypt.hashpw(dto.getPassword(),BCrypt.gensalt());
            user.setPassword(hashPassword);
            userMapper.updateById(user);
            redisTemplate.delete("auth:code:"+dto.getEmail());
            return true;
        }else return false;
    }
    @Override
    public String updateUserAvatar(MultipartFile file, Long userId) {

        // 1. 预检查：先看用户在不在
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException("用户不存在，拒绝上传");
        }
        // 2. 用户存在，再执行昂贵的文件上传操作
        String relativePath = fileUploadService.upload(file, "user/avatar");
         log.info("用户 {} 头像更新成功: {}", userId, relativePath);

        // 3. 返回路径，方便 Controller 告知前端
        return relativePath;
    }
    @Override
    public LoginVO updateUserInfo(UpdateUserInfoDTO dto, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在!");
        User existUser = this.isEmailRegistered(dto.getEmail());
        if (existUser != null && !userId.equals(existUser.getId())) throw new BusinessException("此邮箱已经被注册了");
        existUser = this.isNameRegistered(dto.getName());
        if (existUser != null && !userId.equals(existUser.getId())) throw new BusinessException("此用户名已经被注册了");
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setAvatar(dto.getAvatar());
        userMapper.updateById(user);
        LoginVO vo = new LoginVO();
        BeanUtils.copyProperties(user,vo);
        return vo;
    }

    @Override
    public User seleUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @EventListener
    public void addPostCount(PostCreateEvent event) {
        countService.updateAtomicCount(userMapper, event.creator(),
                "post_count", 1,true);
    }
    @EventListener
    public void reducePostCount(PostDeleteEvent event){
        countService.updateAtomicCount(userMapper, event.creator(),
                "post_count", 1,false);
    }
    @EventListener
    public void restorePostCount(PostRestoreEvent event) {
        countService.updateAtomicCount(userMapper, event.creator(),
                "post_count", 1,true);
    }


}
