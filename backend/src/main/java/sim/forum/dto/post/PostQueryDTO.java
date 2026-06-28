package sim.forum.dto.post;

import lombok.Data;
import sim.forum.dto.PageRequestDTO;

@Data
public class PostQueryDTO extends PageRequestDTO {
    // 社区的ID(可选)
    private Long boardId;
}
