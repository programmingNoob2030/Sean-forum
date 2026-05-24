package sim.forum.dto.message;

import lombok.Data;
import sim.forum.entity.Message;

@Data
public class SendMessageDTO {

    // 发起人的ID
    private Long senderId;

    // 行为
    private Message.Action action;

    // 接收人的ID
    private Long receiverId;

    // 消息附带内容
    private String content;

    // 消息的载体的ID
    private Long targetId;

    // 消息的载体
    private String target;


}
