package sim.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import sim.forum.context.UserContext;
import sim.forum.dto.comment.CommentDTO;
import sim.forum.dto.comment.DeleteCommentDTO;
import sim.forum.dto.comment.GetPostCommentsDTO;
import sim.forum.dto.comment.RestoreCommentDTO;
import sim.forum.entity.Comment;
import sim.forum.event.comment.CommentCreateEvent;
import sim.forum.event.comment.CommentDeleteEvent;
import sim.forum.event.comment.CommentRestoreEvent;
import sim.forum.event.rating.ToggleRatingEvent;
import sim.forum.exception.BusinessException;
import sim.forum.mapper.CommentMapper;
import sim.forum.result.PageResult;
import sim.forum.service.CommentService;
import sim.forum.service.CountService;
import sim.forum.service.PostService;
import sim.forum.vo.comment.CommentVO;

import java.util.*;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private CountService countService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private PostService postService;
    @Override
    public Comment createComment(CommentDTO dto) {
        System.out.println("见来了创建评论的方法");
        Comment comment = new Comment();
        BeanUtils.copyProperties(dto, comment);
        comment.setCreator(UserContext.getUserId());
        commentMapper.insert(comment);

        // 直接对事件进行广播
        System.out.println("更新前帖子的评论数" + postService.selectById(dto.getRootId()).getCommentCount() );
        eventPublisher.publishEvent(new CommentCreateEvent(UserContext.getUserId(), dto.getRootId() , Comment.CommentTarget.POST));
        System.out.println("更新后帖子的评论数" + postService.selectById(dto.getRootId()).getCommentCount() );
        return commentMapper.selectById(comment.getId());
    }

    @Override
    public void deleteComment(DeleteCommentDTO dto) {
        Comment comment = commentMapper.selectById(dto.getId());
        if (comment == null ) throw new BusinessException("此条评论不存在!");
        if (comment.getCreator().equals(UserContext.getUserId())){
            commentMapper.deleteById(comment.getId());
            // 直接对事件进行广播
            eventPublisher.publishEvent(new CommentDeleteEvent(comment.getTargetId(), comment.getTarget()));
        }else throw new BusinessException("您没有资格删除此评论");
    }

    @Override
    public Comment restoreComment(RestoreCommentDTO dto) {
        Comment comment = commentMapper.selectByIdIgnoreDeleted(dto.getId());
        if (comment == null || !comment.getIsDeleted()) throw new BusinessException("要恢复的评论记录不存在!");
        if (comment.getCreator().equals(UserContext.getUserId())){
            comment.setIsDeleted(false);
            commentMapper.updateByIdIgnoreDeleted(comment);
            // 直接对事件进行广播
            eventPublisher.publishEvent(new CommentRestoreEvent(comment.getTargetId(), comment.getTarget()));
            return comment;
        }else throw new BusinessException("你没有资格恢复此条评论记录!");
    }
    @Override
    public Comment selectById(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) throw new BusinessException("此条评论记录不存在");
        return comment;
    }

    @Override
    public PageResult<CommentVO> getCommentsByPostId(GetPostCommentsDTO dto) {
        int num = (dto.getPageNum() == null || dto.getPageNum() <= 0) ? 1 : dto.getPageNum();
        int size = (dto.getPageSize() == null || dto.getPageSize() <= 0) ? 10 : dto.getPageSize();
        // PageHelper分页操作必须紧扣查询语句上方
        PageHelper.startPage(num, size);
        List<CommentVO> list = commentMapper.getCommentsWithRatingCondition(UserContext.getUserId(),dto.getPostId());
        // PageHelper 转换为 PageInfo
        Map<Long, CommentVO> nodeMap = new HashMap<>();
        List<CommentVO> roots = new ArrayList<>();

        for (CommentVO vo : list){
            vo.setChildren(new ArrayList<>());
            nodeMap.put(vo.getId(), vo);
            if (vo.getParentId() == 0){
                roots.add(vo);
            }
        }

        for (CommentVO vo : list){
            if (vo.getParentId() != 0){
                CommentVO parent = nodeMap.get(vo.getParentId());
                if (parent != null){
                    parent.getChildren().add(vo);
                }
            }
        }
        return PageResult.of(new PageInfo<>(roots));
    }

    @Override
    public Integer getCommentLikeCountById(Long commentId){
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException("此评论不存在");
        return comment.getLikeCount();
    }

    @EventListener(condition = "#event.target == T(sim.forum.entity.Rating$RatingType).COMMENT")
    public void updatePostLikeCount(ToggleRatingEvent event) {
        countService.updateAtomicCount(commentMapper, event.targetId(),
                "like_count", event.delta(), true);
    }
}
