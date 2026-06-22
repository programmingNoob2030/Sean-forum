package sim.forum.vo.post;

import lombok.Data;
import sim.forum.entity.Post;

@Data
public class PostVO extends Post {
    // 是否被喜欢
    private Integer postRatingType;

    // 发帖人的昵称
    private String creatorName;

    // 发帖人的头像
    private String creatorAvatar;

    // 所在社区的名称
    private String boardName;

    // 所在社区的封面
    private String boardCover;

    // 所在社区的ID
    private Long boardId;
}
