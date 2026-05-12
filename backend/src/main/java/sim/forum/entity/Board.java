package sim.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
@TableName(value = "boards")
public class Board {
    // 社区的ID
    @TableId(type = IdType.AUTO) // 只有明确写了 AUTO，才会用数据库的自增
    private Long id;

    // 社区的名称
    private String name;

    // 社区的封面
    private String cover;

    public enum BoardType{
        PUBLIC,
        PRIVATE,
        STRICT
    }
    // 社区的类型
    private BoardType type;

    // 社区的描述
    private String description;

    // 社区的背景图
    private String banner;

    // 社区的成员数
    private Integer memberCount;

    // 社区的帖子数
    private Integer postCount;

    // 社区的创建时间
    private Date createTime;

    // 社区的创建者
    private Long creator;
}
