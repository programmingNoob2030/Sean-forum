package sim.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import sim.forum.entity.Comment;
import sim.forum.vo.comment.CommentVO;

import java.util.List;


@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    @Select("select * from comments where id = #{id}")
    Comment selectByIdIgnoreDeleted(Long id);

    @Update("update comments set is_deleted = 0 where id = #{id}")
    void updateByIdIgnoreDeleted(Comment comment);

    List<CommentVO> getCommentsWithRatingCondition(Long userId, Long postId);
}
