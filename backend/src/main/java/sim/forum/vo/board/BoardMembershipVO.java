package sim.forum.vo.board;

import lombok.Data;
import sim.forum.entity.BoardMember;

@Data
public class BoardMembershipVO {
    private Long boardId;
    private BoardMember.RoleType role;
    private Integer memberCount;
}
