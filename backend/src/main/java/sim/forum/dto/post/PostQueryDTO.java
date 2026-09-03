package sim.forum.dto.post;

import lombok.Data;
import sim.forum.dto.PageRequestDTO;

@Data
public class PostQueryDTO extends PageRequestDTO {
    // 社区的ID(可选)
    private Long boardId;

    // 帖子标题或正文搜索关键词(可选)
    private String keyword;

    public enum PostSort{
        RECENT, // 最近
        POPULAR, // 最受欢迎
        COMMENTS, // 评论数
        HOT // 最热门
    }

    // 筛选条件(可选)
    private PostSort sort;



}
