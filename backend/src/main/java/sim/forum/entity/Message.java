package sim.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.Getter;

import java.util.Date;

@TableName("messages")
@Data
public class Message{
    @TableId(type = IdType.AUTO)
    // 消息的ID
    private Long id;

    // 消息发起者的ID
    private Long senderId;

    // 消息的类型
    private Action action;
    
    @Getter
    public enum Action implements IEnum<Integer> {
        LIKE(1,"LIKE","喜欢"),
        DISLIKE(2,"DISLIKE","拉踩"),
        COMMENT(3,"COMMENT","评论");
        @EnumValue
        private final Integer code;
        private final String type;
        private final String description;
        Action(Integer code, String type, String description) {
            this.code = code;
            this.type = type;
            this.description = description;
        }

        // 给前端看的值
        @JsonValue
        public String getType(){
            return this.type;
        }

        // 给MyBatisPlus看的值
        @Override
        public Integer getValue() {
            return this.code;
        }
    }

    // 消息的载体
    private Target target;
    @Getter
    public enum Target implements IEnum<Integer> {
        POST(1, "POST", "帖子"),
        COMMENT(2, "COMMENT", "评论");

        @EnumValue
        private final Integer code;
        private final String type;
        private final String description;

        Target(Integer code, String type, String description) {
            this.code = code;
            this.type = type;
            this.description = description;
        }

        @JsonValue
        public String getType() { return this.type; }

        @Override
        public Integer getValue() { return this.code; }
    }

    // 消息载体(POST|COMMENT)的ID
    private Long targetId;

    // 消息接收者的ID
    private Long receiverId;

    // 消息已读与否
    private Boolean isRead;

    // 消息的固定展示文本
    private String content;

    // 消息的创建时间
    private Date createTime;



}
