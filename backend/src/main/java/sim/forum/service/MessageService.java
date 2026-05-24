package sim.forum.service;

import org.springframework.web.bind.annotation.RequestBody;
import sim.forum.dto.PageRequestDTO;
import sim.forum.dto.message.SendMessageDTO;
import sim.forum.result.PageResult;
import sim.forum.vo.message.MessageVO;

public interface MessageService {
    void send(SendMessageDTO dto);

    Integer getUserUnreadCount(Long userId);

    PageResult<MessageVO> getUserMessages(@RequestBody PageRequestDTO dto, Long userId);
}
