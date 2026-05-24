package sim.forum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sim.forum.context.UserContext;
import sim.forum.dto.PageRequestDTO;
import sim.forum.result.PageResult;
import sim.forum.result.Result;
import sim.forum.service.MessageService;
import sim.forum.vo.message.MessageVO;

@RestController
public class MessageController {
    @Autowired
    private MessageService messageService;
    @GetMapping("/message/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = UserContext.getUserId();
        Integer count = messageService.getUserUnreadCount(userId);
        return Result.success(count);
    }

    @GetMapping("/messages")
    public Result<PageResult<MessageVO>> getCommentMessages(PageRequestDTO dto) {
        Long userId = UserContext.getUserId();
        PageResult<MessageVO> list = messageService.getUserMessages(dto,userId);
        return Result.success(list);
    }
}
