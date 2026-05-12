package sim.forum.vo.board;

import lombok.Data;
import sim.forum.entity.BoardMember;

@Data
public class BriefBoardVO {
    // 社区的ID
    private Long id;

    // 社区的名称
    private String name;

    // 社区的封面
    private String cover;

    // 成员的类型
    private BoardMember.RoleType role;
}
