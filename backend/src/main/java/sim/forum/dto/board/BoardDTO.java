package sim.forum.dto.board;

import lombok.Data;
import sim.forum.entity.Board;

@Data
public class BoardDTO {
    // 社区的名称
    private String name;
    // 社区的封面
    private String cover;
    // 社区的类型
    private Board.BoardType type;
    // 社区的描述
    private String description;
}
