package sim.forum.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sim.forum.annotation.OptionalAuth;
import sim.forum.context.UserContext;
import sim.forum.dto.comment.GetPostCommentsDTO;
import sim.forum.result.PageResult;
import sim.forum.result.Result;
import sim.forum.dto.comment.CommentDTO;
import sim.forum.dto.comment.DeleteCommentDTO;
import sim.forum.dto.comment.RestoreCommentDTO;
import sim.forum.entity.Comment;
import sim.forum.service.CommentService;
import sim.forum.vo.comment.CommentVO;

import java.util.List;

@RestController
public class CommentController {
    @Autowired
    private CommentService commentService;
    @PostMapping("/comments")
    public Result<Comment> createComment(@Valid @RequestBody CommentDTO dto){
        Long userId = UserContext.getUserId();
        Comment comment = commentService.createComment(dto, userId);
        return Result.success(comment);
    }
    @DeleteMapping("/comments")
    public Result<String> deleteComment(@Valid @RequestBody DeleteCommentDTO dto){
        commentService.deleteComment(dto);
        return Result.success("删除评论成功!");
    }
    @PatchMapping("/comments")
    public Result<Comment> restoreComment(@Valid @RequestBody RestoreCommentDTO dto){
        Comment comment = commentService.restoreComment(dto);
        return Result.success(comment);
    }

    @GetMapping("/post-comments")
    @OptionalAuth
    public Result<PageResult<CommentVO>> getCommentsByPostId(GetPostCommentsDTO dto){
        Long userId = UserContext.getUserId();
        PageResult<CommentVO> comments = commentService.getCommentsByPostId(dto, userId);
        return Result.success(comments);
    }
}
