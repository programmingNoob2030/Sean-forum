package sim.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("board_members")
public class BoardMember {
    // 此条记录的ID
    @TableId(type = IdType.AUTO)
    private Long id;

    // 所属社区的ID
    private Long boardId;

    // 加入的成员ID
    private Long memberId;

    // 成员的角色
    private RoleType role;
    public enum RoleType{
        MEMBER,
        ADMIN,
        CREATOR
    }

    // 成员加入的时间
    private LocalDateTime createTime;
}