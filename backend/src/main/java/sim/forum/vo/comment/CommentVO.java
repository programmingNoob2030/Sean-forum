package sim.forum.vo.comment;

import lombok.Data;
import sim.forum.entity.Comment;

import java.util.List;

@Data
public class CommentVO extends Comment {
    // 创建人的名字
    private String creatorName;

    // 创建人的头像
    private String creatorAvatar;

    // 我的评价
    private Integer commentRatingType;

    // 子评论
    private List<CommentVO> children;
}
