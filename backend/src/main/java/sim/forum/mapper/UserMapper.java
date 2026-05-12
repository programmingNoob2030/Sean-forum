package sim.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sim.forum.dto.user.LoginDTO;
import sim.forum.entity.User;
import sim.forum.vo.user.LoginVO;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    LoginVO loginResult(LoginDTO dto);
}
