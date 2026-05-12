package sim.forum.dto.comment;

import lombok.Data;
import sim.forum.entity.Comment;

@Data
public class CommentDTO {

    // 评论的对象
    private Comment.CommentTarget target;

    // 评论的的对象ID
    private Long targetId;

    // 评论的内容
    private String content;

    // 评论的资源的ID
    private Long rootId;

    // 评论的资源的类型
    private Comment.CommentTarget rootType;

    // 顶级评论的ID
    private Long parentId;
}
