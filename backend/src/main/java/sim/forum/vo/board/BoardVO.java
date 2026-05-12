package sim.forum.vo.board;

import lombok.Data;
import sim.forum.entity.Board;

@Data
public class BoardVO extends Board {
    // 当前用户的类型
    private String currentUserRole;

    // 创建者的名称
    private String creatorName;

    // 创建者的头像
    private String creatorAvatar;
}
