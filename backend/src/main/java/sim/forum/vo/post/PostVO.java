package sim.forum.vo.post;

import lombok.Data;
import sim.forum.dto.post.content.PostContentFormat;
import sim.forum.entity.Post;
import sim.forum.vo.post.content.PostContentNodeVO;

import java.util.List;

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

    // 正文格式
    private PostContentFormat contentFormat;

    // 正文节点
    private List<PostContentNodeVO> contentNodes;

    // 正文文本预览
    private String contentTextPreview;

    // 第一张图片路径
    private String firstImagePath;
}
