package sim.forum.dto.post;

import lombok.Data;
import sim.forum.dto.PageRequestDTO;

@Data
public class PostQueryDTO extends PageRequestDTO {
    // 社区的ID(可选)
    private Long boardId;

    public enum PostSort{
        RECENT, // 最近
        POPULAR, // 最受欢迎
        COMMENTS, // 评论数
        HOT // 最热门
    }

    // 筛选条件(可选)
    private PostSort sort;



}
