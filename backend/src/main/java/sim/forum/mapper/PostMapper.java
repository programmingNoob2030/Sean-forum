package sim.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import sim.forum.dto.post.PostQueryDTO;
import sim.forum.entity.Post;
import sim.forum.vo.board.BriefBoardVO;
import sim.forum.vo.post.PostVO;
import sim.forum.vo.post.RecentPostVO;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Select("select * from posts where id = #{id}")
    Post selectByIdIgnoreDeleted(Long id);

    @Update("update posts set is_deleted = 0 where id = #{id}")
    void updateByIdIgnoreDeleted(Post post);

    List<PostVO> getPosts(PostQueryDTO dto, Long userId);

    PostVO getPostWithRatingConditionById(Long userId, Long postId);

    List<RecentPostVO> getRecentPosts(List<Long> ids, Long userId);

}
