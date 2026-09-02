package sim.forum.vo.board;

import lombok.Data;
import sim.forum.entity.BoardMember;

@Data
public class SquareBoardVO {
    // 社区的ID
    private Long id;

    // 社区的名称
    private String name;

    // 社区的封面
    private String cover;

    // 社区的描描述
    private String description;

    private Integer memberCount;

    // 社区的周访客数
    private Long weeklyVisitor;

    // 当前用户和展示社区的关系
    private BoardMember.RoleType role;
}
