package sim.forum.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sim.forum.context.UserContext;
import sim.forum.dto.comment.CommentDTO;
import sim.forum.dto.comment.DeleteCommentDTO;
import sim.forum.dto.comment.RestoreCommentDTO;
import sim.forum.entity.Comment;
import sim.forum.exception.BusinessException;

@SpringBootTest
public class CommentServiceTest {
    @Autowired
    private CommentService commentService;

    @Test
    void testCreateComment(){
        CommentDTO dto = new CommentDTO();
        UserContext.setUserId(11L);
        dto.setContent("This is a comment for post No.20.");
        dto.setTarget(Comment.CommentTarget.POST);


        // 是否执行操作
        Comment comment = commentService.createComment(dto, UserContext.getUserId());
        Assertions.assertNotNull(comment);

        // 是否创建成功
        Long id = comment.getId();
        Assertions.assertNotNull(id);
    }

    @Test
    void testDeleteComment(){
        DeleteCommentDTO dto = new DeleteCommentDTO();
        UserContext.setUserId(11L);
        dto.setId(11L);
        Comment comment = commentService.selectById(dto.getId());
        // 确认喜欢被删除之前是存在的
        Assertions.assertNotNull(comment);
        // 删除
        commentService.deleteComment(dto);
        // 2. 断言：调用查询时【必须】抛出 BusinessException
        // 如果 selectById 没抛异常，说明没删掉，测试反而会失败
        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> {
            commentService.selectById(dto.getId()); // 假设这是你封装的带异常检查的查询
        });
    }

    @Test
    void testRestoreComment(){
        UserContext.setUserId(11L);

        RestoreCommentDTO dto = new RestoreCommentDTO();
        dto.setId(13L);
//        Post p = postService.selectById(dto.getId());
        // 确认p被恢复之前是不存在的
//        Assertions.assertNull(p);
        // 恢复
        commentService.restoreComment(dto);
        // 是否恢复成功
        Comment comment = commentService.selectById(dto.getId());
        Assertions.assertNotNull(comment);
    }

}
