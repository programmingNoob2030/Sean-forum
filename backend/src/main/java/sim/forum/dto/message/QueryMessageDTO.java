package sim.forum.dto.message;

import lombok.Data;
import sim.forum.dto.PageRequestDTO;
import sim.forum.entity.Message;

import java.util.List;

@Data
public class QueryMessageDTO extends PageRequestDTO {
    // 查询消息的类型
    private List<Message.Action> actions;
}
