package sim.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@TableName("posts")
@Data
public class Post {
    @TableId(type = IdType.AUTO)
    /* 帖子的ID */
    private Long id;

    /* 帖子的发布者 */
    private Long creator;

    /* 帖子所属的社区 */
    private Long boardId;

    /* 帖子的标题 */
    private String title;

    /* 帖子的内容 */
    private String content;

    @TableField("is_deleted")  // 确保能映射到数据库的 is_deleted 列
    @TableLogic(value = "0", delval = "1") // 显式指定 0 是存，1 是删，防备全局配置失效
   /* 帖子的状态 */
    private Boolean isDeleted;

    /* 帖子的发布时间 */
    private Date createTime;

    /* 帖子的点赞数 */
    private Integer likeCount;

    /* 帖子的评论数 */
    private Integer commentCount;
}
