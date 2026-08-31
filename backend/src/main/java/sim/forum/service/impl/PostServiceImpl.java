package sim.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import sim.forum.context.UserContext;
import sim.forum.dto.PageRequestDTO;
import sim.forum.dto.browserecord.CreateBrowseRecordDTO;
import sim.forum.dto.post.CreatePostDTO;
import sim.forum.dto.post.DeletePostDTO;
import sim.forum.dto.post.PostQueryDTO;
import sim.forum.dto.post.RestorePostDTO;
import sim.forum.dto.post.content.PostContentCodec;
import sim.forum.dto.post.content.PostContentDocument;
import sim.forum.entity.BrowseRecord;
import sim.forum.entity.Post;
import sim.forum.event.comment.CommentCreateEvent;
import sim.forum.event.comment.CommentDeleteEvent;
import sim.forum.event.comment.CommentRestoreEvent;
import sim.forum.event.post.PostCreateEvent;
import sim.forum.event.post.PostDeleteEvent;
import sim.forum.event.post.PostRestoreEvent;
import sim.forum.event.rating.ToggleRatingEvent;
import sim.forum.exception.BusinessException;
import sim.forum.mapper.PostMapper;
import sim.forum.result.PageResult;
import sim.forum.service.BrowseRecordService;
import sim.forum.service.CountService;
import sim.forum.service.PostService;
import sim.forum.vo.post.PostVO;
import sim.forum.vo.post.RecentPostVO;

import java.util.List;

@Transactional
@Service
@Validated
public class PostServiceImpl implements PostService {
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CountService countService;
    @Autowired
    private BrowseRecordService browseRecordService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private PostContentCodec postContentCodec;
    @Override
    public Post createPost(CreatePostDTO dto, Long userId) {
        postContentCodec.parseForWrite(dto.getContent());

        Post p = new Post();
        p.setCreator(userId);

        p.setTitle(dto.getTitle());
        p.setContent(dto.getContent());

        p.setBoardId(dto.getBoardId());

        postMapper.insert(p);
        // 直接对事件进行广播
        eventPublisher.publishEvent(new PostCreateEvent(userId, dto.getBoardId()));
        return postMapper.selectById(p.getId());
    }

    @Override
    public void deletePost(DeletePostDTO dto) {
        Post post = postMapper.selectById(dto.getId());
        if (post == null ) throw new BusinessException("要删除的帖子不存在!");
        if (post.getCreator().equals(UserContext.getUserId())){
            postMapper.deleteById(post.getId());
            // 直接对事件进行广播
            eventPublisher.publishEvent(new PostDeleteEvent(post.getCreator()));
        }else throw new BusinessException("您没有资格删除此帖子!");
    }

    @Override
    public Post restorePost(RestorePostDTO dto) {
        // MyBatisPlus原生的select方法被逻辑注解影响判断post的真实存在情况
        // Post post = postMapper.selectById(dto.getId());
        // 我们手动写一个无视isDeleted字段的select方法
        Post post = postMapper.selectByIdIgnoreDeleted(dto.getId());
        if (post == null || !post.getIsDeleted()) throw new BusinessException("要恢复的帖子不存在!");
        if (post.getCreator().equals(UserContext.getUserId())){
            post.setIsDeleted(false);
            postMapper.updateByIdIgnoreDeleted(post);
            // 直接对事件进行广播
            eventPublisher.publishEvent(new PostRestoreEvent(post.getCreator()));
            return post;
        }else throw new BusinessException("您没有资格恢复此帖子!");
    }

    @Override
    public Post selectById(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) throw new BusinessException("此帖子不存在!");
        return post;
    }

    @Override
    public PostVO getPostById(Long userId, Long postId) {
        PostVO postVO = postMapper.getPostWithRatingConditionById(userId, postId);
        CreateBrowseRecordDTO dto = new CreateBrowseRecordDTO();
        dto.setTarget(BrowseRecord.BrowseTarget.POST);
        dto.setTargetId(postId);
        browseRecordService.saveRecordAsync(dto, userId);
        if (postVO == null) throw new BusinessException("没有找到此条帖子");
        enrichPostContent(postVO);
        return postVO;
    }

    @Override
    public PageResult<PostVO> getPosts(@RequestBody PostQueryDTO dto) {
        int num = (dto.getPageNum() == null || dto.getPageNum() <= 0) ? 1 : dto.getPageNum();
        int size = (dto.getPageSize() == null || dto.getPageSize() <= 0) ? 10 : dto.getPageSize();

        // PageHelper分页操作必须紧扣查询语句上方
        PageHelper.startPage(num, size);
        List<PostVO> list;
        if (dto.getSort() == PostQueryDTO.PostSort.HOT) {
            list = postMapper.getHotPosts(dto, UserContext.getUserId());
        } else {
            list = postMapper.getPosts(dto, UserContext.getUserId());
        }
        if (list == null || list.isEmpty()) throw new BusinessException("当前没有任何帖子!");
        list.forEach(this::enrichPostContent);

        // PageHelper 转换为 PageInfo
        return PageResult.of(new PageInfo<>(list));
    }

    @Override
    public List<RecentPostVO> getRecentPosts(List<Long> ids, Long userId) {
        if (userId == null) throw new BusinessException("此用户信息有误,无法查询帖子信息");
        List<RecentPostVO> recentPosts = postMapper.getRecentPosts(ids, userId);
        if (recentPosts != null) {
            recentPosts.forEach(this::enrichRecentPostContent);
        }
        return recentPosts;
    }


    @Override
    public Integer getPostLikeCountById(Long postId){
        Post p = postMapper.selectById(postId);
        if (p == null) throw new BusinessException("此帖子不存在");
        return p.getLikeCount();
    }

    @Override
    public Long getUserIdByPostId(Long postId) {
        Post p = postMapper.selectById(postId);
        return p == null ? null :  p.getCreator();
    }

    @EventListener(condition = "#event.target == T(sim.forum.entity.Rating$RatingType).POST")
    public void updatePostLikeCount(ToggleRatingEvent event) {
        countService.updateAtomicCount(postMapper, event.targetId(),
                "like_count", event.delta(), true);
    }
    @EventListener(condition = "#event.target == T(sim.forum.entity.Comment$CommentTarget).POST")
    public void addPostCommentCount(CommentCreateEvent event) {
        countService.updateAtomicCount(postMapper, event.targetId(),
                "comment_count", 1,true);
    }
    @EventListener(condition = "#event.target == T(sim.forum.entity.Comment$CommentTarget).POST")
    public void reducePostCommentCount(CommentDeleteEvent event) {
        countService.updateAtomicCount(postMapper, event.targetId(),
                "comment_count", 1,false);
    }
    @EventListener(condition = "#event.target == T(sim.forum.entity.Comment$CommentTarget).POST")
    public void restorePostCommentCount(CommentRestoreEvent event) {
        countService.updateAtomicCount(postMapper, event.targetId(),
                "comment_count", 1,true);
    }

    private void enrichPostContent(PostVO postVO) {
        PostContentDocument document = postContentCodec.normalizeForRead(postVO.getContent());
        postVO.setContentFormat(document.getFormat());
        postVO.setContentNodes(postContentCodec.toNodeVoList(document));
        postVO.setContentTextPreview(document.getTextPreview());
        postVO.setFirstImagePath(document.getFirstImagePath());
    }

    private void enrichRecentPostContent(RecentPostVO recentPostVO) {
        PostContentDocument document = postContentCodec.normalizeForRead(recentPostVO.getContent());
        recentPostVO.setContentFormat(document.getFormat());
        recentPostVO.setContentTextPreview(document.getTextPreview());
        recentPostVO.setFirstImagePath(document.getFirstImagePath());
    }
}
