package sim.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sim.forum.entity.Message;
import sim.forum.vo.message.MessageVO;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    List<MessageVO> getUserMessages(Long userId);
}
