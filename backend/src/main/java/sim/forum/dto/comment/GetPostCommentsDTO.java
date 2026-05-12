package sim.forum.dto.comment;

import lombok.Data;
import sim.forum.dto.PageRequestDTO;

@Data
public class GetPostCommentsDTO extends PageRequestDTO {

    // 帖子的ID
    private Long postId;
}
