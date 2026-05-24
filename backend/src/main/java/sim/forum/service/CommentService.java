package sim.forum.service;

import sim.forum.dto.comment.CommentDTO;
import sim.forum.dto.comment.DeleteCommentDTO;
import sim.forum.dto.comment.GetPostCommentsDTO;
import sim.forum.dto.comment.RestoreCommentDTO;
import sim.forum.entity.Comment;
import sim.forum.result.PageResult;
import sim.forum.vo.comment.CommentVO;

import java.util.List;

public interface CommentService {
    Comment createComment(CommentDTO dto, Long userId);

    void deleteComment(DeleteCommentDTO dto);

    Comment restoreComment(RestoreCommentDTO dto);
    Comment selectById(Long id);


    PageResult<CommentVO> getCommentsByPostId(GetPostCommentsDTO dto);

    Integer getCommentLikeCountById(Long commentId);

    Long getUserIdByCommentId(Long commentId);
}
