package sim.forum.vo.post;

import lombok.Data;
import sim.forum.dto.post.content.PostContentFormat;

import java.util.Date;

@Data
public class RecentPostVO {

    /**
     * 帖子的ID
     */
    private Long id;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容摘要
     */
    private String content;

    /**
     * 正文格式
     */
    private PostContentFormat contentFormat;

    /**
     * 正文文本预览
     */
    private String contentTextPreview;

    /**
     * 第一张图片路径
     */
    private String firstImagePath;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 所属板块名称
     */
    private String boardName;

    /**
     * 所属板块封面图URL
     */
    private String boardCover;
}
