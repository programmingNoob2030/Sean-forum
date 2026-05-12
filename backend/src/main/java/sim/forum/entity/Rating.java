package sim.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;
@TableName("ratings")
@Data
public class Rating {
    @TableId(type = IdType.AUTO)
    /* 评价的ID */
    private Long id;

    /* 评价的人 */
    private Long creator;

    /* 评价的对象 */
    private RatingType target;
    public enum RatingType{
        COMMENT,
        USER,
        POST
    }

    /* 评价的对象的ID */
    private Long targetId;

    /* 喜欢的时间 */
    private Date createTime;

    /* 评价的类型 0拉踩 1点赞 */
    private Integer type;
}
