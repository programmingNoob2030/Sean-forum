package sim.forum.vo.message;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import sim.forum.entity.Message;

import java.util.Date;

@Data
public class MessageVO {
    // 信息的ID
    private Long id;
    // 用户名
    private String name;

    // 用户头像
    private String avatar;

    // 类型
    private Message.Action action;

    // 载体
    private String target;

    // 载体的ID
    private Long targetId;

    // 内容
    private String content;

    // 原内容
    private String reference;

    // 时间
    private Date createTime;

    // 是否已读
    @TableField("is_read")
    private Boolean isRead;
}
