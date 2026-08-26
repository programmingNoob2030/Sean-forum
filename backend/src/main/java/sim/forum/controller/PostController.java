package sim.forum.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sim.forum.annotation.OptionalAuth;
import sim.forum.context.UserContext;
import sim.forum.dto.post.PostQueryDTO;
import sim.forum.entity.BrowseRecord;
import sim.forum.result.PageResult;
import sim.forum.result.Result;
import sim.forum.dto.post.CreatePostDTO;
import sim.forum.dto.post.DeletePostDTO;
import sim.forum.dto.post.RestorePostDTO;
import sim.forum.entity.Post;
import sim.forum.service.BrowseRecordService;
import sim.forum.service.FileUploadService;
import sim.forum.service.PostService;
import sim.forum.vo.post.PostVO;
import sim.forum.vo.post.RecentPostVO;

import java.util.List;

@RestController
public class PostController {
    @Autowired
    private PostService postService;
    @Autowired
    private BrowseRecordService browseRecordService;
    @Autowired
    private FileUploadService fileUploadService;

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

    @PostMapping("/posts/images")
    public Result<String> uploadPostContentImage(@RequestParam("file") MultipartFile file) {
        Long userId = UserContext.getUserId();
        String imagePath = fileUploadService.uploadPostContentImage(file, userId);
        return Result.success(imagePath);
    }

    @GetMapping("/posts/history")
    public Result<List<RecentPostVO>> getRecentPosts(){
        Long userId = UserContext.getUserId();
        List<Long> ids = browseRecordService.getIdsFromRedis(BrowseRecord.BrowseTarget.POST, userId);
        List<RecentPostVO> list = postService.getRecentPosts(ids, userId);
        return Result.success(list);
    }
    @DeleteMapping("/posts/history")
    public Result<Void> deleteRecentPosts(){
        Long userId = UserContext.getUserId();
        browseRecordService.deletePostRecordAsync(userId);
        return Result.success();
    }
    @GetMapping("/posts")
    @OptionalAuth
    public Result<PageResult<PostVO>> getPosts(PostQueryDTO dto){
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
