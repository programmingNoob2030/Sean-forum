package sim.forum.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import sim.forum.context.UserContext;
import sim.forum.dto.comment.CommentDTO;
import sim.forum.dto.comment.DeleteCommentDTO;
import sim.forum.dto.comment.RestoreCommentDTO;
import sim.forum.entity.Comment;
import sim.forum.exception.BusinessException;

@SpringBootTest
@Transactional
public class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @MockitoBean
    private MailService mailService;

    private static final Long TEST_USER_ID = 19L;
    private static final Long TEST_POST_ID = 17815L;

    private CommentDTO createCommentDTO(String content) {
        CommentDTO dto = new CommentDTO();
        dto.setContent(content);
        dto.setTarget(Comment.CommentTarget.POST);
        dto.setTargetId(TEST_POST_ID);
        dto.setRootId(TEST_POST_ID);
        dto.setRootType(Comment.CommentTarget.POST);
        dto.setParentId(0L);
        return dto;
    }

    @AfterEach
    void clearContext() {
        UserContext.removeUserId();
    }

    @Test
    void testCreateComment() {
        UserContext.setUserId(TEST_USER_ID);

        Comment comment = commentService.createComment(
                createCommentDTO("test comment"),
                TEST_USER_ID
        );

        Assertions.assertNotNull(comment);
        Assertions.assertNotNull(comment.getId());
        Assertions.assertEquals("test comment", comment.getContent());
    }

    @Test
    void testCreateCommentRejectBlank() {
        UserContext.setUserId(TEST_USER_ID);

        BusinessException exception = Assertions.assertThrows(
                BusinessException.class,
                () -> commentService.createComment(
                        createCommentDTO("   "),
                        TEST_USER_ID
                )
        );

        Assertions.assertEquals("评论内容不能为空", exception.getMessage());
    }

    @Test
    void testCreateEmojiComment() {
        UserContext.setUserId(TEST_USER_ID);

        Comment comment = commentService.createComment(
                createCommentDTO("😀"),
                TEST_USER_ID
        );

        Assertions.assertNotNull(comment);
        Assertions.assertEquals("😀", comment.getContent());
    }

    @Test
    void testDeleteComment() {
        UserContext.setUserId(TEST_USER_ID);

        Comment comment = commentService.createComment(
                createCommentDTO("delete test"),
                TEST_USER_ID
        );

        DeleteCommentDTO dto = new DeleteCommentDTO();
        dto.setId(comment.getId());

        commentService.deleteComment(dto);

        Assertions.assertThrows(
                BusinessException.class,
                () -> commentService.selectById(comment.getId())
        );
    }

    @Test
    void testRestoreComment() {
        UserContext.setUserId(TEST_USER_ID);

        Comment comment = commentService.createComment(
                createCommentDTO("restore test"),
                TEST_USER_ID
        );

        DeleteCommentDTO deleteDTO = new DeleteCommentDTO();
        deleteDTO.setId(comment.getId());

        commentService.deleteComment(deleteDTO);

        RestoreCommentDTO restoreDTO = new RestoreCommentDTO();
        restoreDTO.setId(comment.getId());

        commentService.restoreComment(restoreDTO);

        Comment restored = commentService.selectById(comment.getId());

        Assertions.assertNotNull(restored);
        Assertions.assertEquals("restore test", restored.getContent());
    }
}