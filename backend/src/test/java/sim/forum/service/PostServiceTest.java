package sim.forum.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import sim.forum.context.UserContext;
import sim.forum.dto.post.CreatePostDTO;
import sim.forum.dto.post.DeletePostDTO;
import sim.forum.dto.post.RestorePostDTO;
import sim.forum.entity.Post;
import sim.forum.exception.BusinessException;

@SpringBootTest // 如果需要用到数据库或依赖注入，带上它
public class PostServiceTest {
    @Autowired
    private PostService postService;

    // 单元测试环境和SpringMVC不同，不经过拦截器
    // 无法使用全局的Context，需要自己手动设置
    // 否则不知道ID(操作人身份)

    /* 发布帖子 */
    @Test
    void testCreatePost(){
        UserContext.setUserId(13L);
        for (int i = 1; i <= 8888; i++){
            CreatePostDTO dto = new CreatePostDTO();
            dto.setTitle("test title" + i);
            dto.setContent("test content" + i);
            // 是否执行操作
            Post post = postService.createPost(dto);
            Assertions.assertNotNull(post);

            // 是否创建成功
            Long id = post.getId();
            Assertions.assertNotNull(id);
        }

    }

    /* 删除帖子 */
    @Test
    void testDeletePost(){
        UserContext.setUserId(11L);

        DeletePostDTO dto = new DeletePostDTO();
        dto.setId(19L);
        Post p = postService.selectById(dto.getId());
        // 确认p被删除之前是存在的
        Assertions.assertNotNull(p);
        // 删除
        postService.deletePost(dto);
        // 2. 断言：调用查询时【必须】抛出 BusinessException
        // 如果 selectById 没抛异常，说明没删掉，测试反而会失败
        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> {
            postService.selectById(dto.getId()); // 假设这是你封装的带异常检查的查询
        });
    }

    /* 恢复帖子 */
    @Test
    void testRestorePost(){
        UserContext.setUserId(11L);

        RestorePostDTO dto = new RestorePostDTO();
        dto.setId(19L);
//        Post p = postService.selectById(dto.getId());
        // 确认p被恢复之前是不存在的
//        Assertions.assertNull(p);
        // 恢复
        postService.restorePost(dto);
        // 是否恢复成功
        Post p = postService.selectById(dto.getId());
        Assertions.assertNotNull(p);
    }

}
