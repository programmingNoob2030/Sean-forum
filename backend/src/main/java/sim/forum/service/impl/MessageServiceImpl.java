package sim.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import sim.forum.dto.PageRequestDTO;
import sim.forum.dto.message.SendMessageDTO;
import sim.forum.entity.Message;
import sim.forum.exception.BusinessException;
import sim.forum.mapper.MessageMapper;
import sim.forum.result.PageResult;
import sim.forum.service.MessageService;
import sim.forum.vo.message.MessageVO;

import java.util.List;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    @Async("taskExecutor")
    public void send(SendMessageDTO dto) {
        Message message = new Message();
        message.setAction(dto.getAction());
        message.setContent(dto.getContent());
        message.setSenderId(dto.getSenderId());
        message.setReceiverId(dto.getReceiverId());
        message.setTargetId(dto.getTargetId());
        // 🎯 核心改变：用 valueOf 把 String 类型的 "POST" 或 "COMMENT" 还原成实体认的枚举
        if (dto.getTarget() != null) {
            // toUpperCase().trim() 是为了防止前端传参不规范（比如传了小写 "post"），增强鲁棒性
            Message.Target type = Message.Target.valueOf(dto.getTarget().toUpperCase().trim());
            message.setTarget(type);
        }
        if (dto.getReceiverId() == null || dto.getReceiverId().equals(dto.getSenderId())) {
            return;
        }
        messageMapper.insert(message);
        String redisKey = "user:unread:count";
        String field = String.valueOf(dto.getReceiverId());
        // opsForHash().increment(key, field, delta) 是原子操作，高并发下绝不会发生线程安全问题
        stringRedisTemplate.opsForHash().increment(redisKey, field, 1);
    }

    @Override
    public Integer getUserUnreadCount(Long userId) {
        String redisKey = "user:unread:count";
        String field = String.valueOf(userId);
        Object count = stringRedisTemplate.opsForHash().get(redisKey, field);
        return count == null ? 0 : Integer.parseInt(count.toString());
    }

    @Override
    public PageResult<MessageVO> getUserMessages(@RequestBody PageRequestDTO dto, Long userId) {
        int num = (dto.getPageNum() == null || dto.getPageNum() <= 0) ? 1 : dto.getPageNum();
        int size = (dto.getPageSize() == null || dto.getPageSize() <= 0) ? 10 : dto.getPageSize();

        // PageHelper分页操作必须紧扣查询语句上方
        PageHelper.startPage(num, size);
        List<MessageVO> list = messageMapper.getUserMessages(userId);
        return PageResult.of(new PageInfo<>(list));
    }
}

