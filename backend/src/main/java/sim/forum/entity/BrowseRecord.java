package sim.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Getter;

import java.util.Date;

@Data
@TableName("browse_records")
public class BrowseRecord {

    @TableId(type = IdType.AUTO)
    // 记录的ID
    private Long id;

    // 用户的ID
    private Long userId;

    // 用户访问的目标
    private BrowseTarget target;

    @Getter
    public enum BrowseTarget{
        // 1. 定义实例并传入权值 (这里就是你说的 Reddit 逻辑)
        BOARD(5),
        POST(9);

        // 2. 这里的变量就是“随身小书包”
        private final int limit;

        // 3. 构造函数（必须是私有的，或者默认）
        BrowseTarget(int limit) {
            this.limit = limit;
        }
    }

    // 用户访问的目标的ID
    private Long targetId;

    // 用户记录的创建时间
    private Date createTime;
}
