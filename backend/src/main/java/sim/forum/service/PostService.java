package sim.forum.service;

import org.springframework.web.bind.annotation.RequestBody;
import sim.forum.dto.PageRequestDTO;
import sim.forum.dto.post.CreatePostDTO;
import sim.forum.dto.post.DeletePostDTO;
import sim.forum.dto.post.RestorePostDTO;
import sim.forum.entity.BrowseRecord;
import sim.forum.entity.Post;
import sim.forum.result.PageResult;
import sim.forum.vo.post.PostVO;
import sim.forum.vo.post.RecentPostVO;

import java.util.List;


public interface PostService {
    Post createPost(CreatePostDTO dto, Long userId);

    void deletePost(DeletePostDTO dto);

    Post restorePost(RestorePostDTO dto);

    PageResult<PostVO> getPosts(@RequestBody PageRequestDTO dto);

    PostVO getPostById(Long userId, Long postId);

    Post selectById(Long id);

    Integer getPostLikeCountById(Long postId);

    List<RecentPostVO> getRecentPosts(List<Long> ids, Long userId);

    Long getUserIdByPostId(Long postId);
}
