package sim.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;
@TableName("comments")
@Data
public class Comment {
    @TableId(type = IdType.AUTO)
    /* 评论的ID */
    private Long id;

    /* 评论的人 */
    private Long creator;

    /* 评论的对象 */
    private CommentTarget target;
    public enum CommentTarget{
        COMMENT,
        USER,
        POST,
        EMPTY
    }

    /* 评论的对象的ID */
    private Long targetId;

    /* 评论的内容 */
    private String content;

    /* 评论的时间 */
    private Date createTime;

    @TableField(value = "is_deleted")
    @TableLogic(value = "0", delval = "1")
    /* 评论的状态 */
    private Boolean isDeleted;

    /* 评论的根节点ID */
    private Long rootId;

    /* 评论的根节点类型 */
    private CommentTarget rootType;


    /* 顶级评论的ID */
    private Long parentId;

    /* 评论的喜欢数 */
    private Integer likeCount;
}
