package sim.forum.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sim.forum.annotation.OptionalAuth;
import sim.forum.context.UserContext;
import sim.forum.dto.PageRequestDTO;
import sim.forum.mapper.PostMapper;
import sim.forum.result.PageResult;
import sim.forum.result.Result;
import sim.forum.dto.post.CreatePostDTO;
import sim.forum.dto.post.DeletePostDTO;
import sim.forum.dto.post.RestorePostDTO;
import sim.forum.entity.Post;
import sim.forum.service.PostService;
import sim.forum.vo.post.PostVO;

@RestController
public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping("/posts")
    public Result<Post> createPost(@Valid @RequestBody CreatePostDTO dto){
        Long userId = UserContext.getUserId();
        Post p = postService.createPost(dto, userId);
        return Result.success(p);
    }

    @DeleteMapping("/posts")
    public Result<String> deletePost(@Valid @RequestBody DeletePostDTO dto){
        postService.deletePost(dto);
        return Result.success("帖子删除成功!");
    }
    @PatchMapping("/posts")
    public Result<Post> restorePost(@Valid @RequestBody RestorePostDTO dto){
        Post post = postService.restorePost(dto);
        return Result.success(post);
    }
    @GetMapping("/posts")
    @OptionalAuth
    public Result<PageResult<PostVO>> getPosts(PageRequestDTO dto){
        PageResult<PostVO> posts = postService.getPosts(dto);
        return Result.success(posts);
    }
    @GetMapping("/post/{id}")
    @OptionalAuth
    public Result<PostVO> getPostById(@PathVariable Long id){
        PostVO postVO = postService.getPostById(UserContext.getUserId(), id);
        return Result.success(postVO);
    }
}
