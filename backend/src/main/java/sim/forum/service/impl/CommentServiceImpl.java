package sim.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sim.forum.context.UserContext;
import sim.forum.dto.comment.CommentDTO;
import sim.forum.dto.comment.DeleteCommentDTO;
import sim.forum.dto.comment.GetPostCommentsDTO;
import sim.forum.dto.comment.RestoreCommentDTO;
import sim.forum.dto.message.SendMessageDTO;
import sim.forum.entity.Comment;
import sim.forum.entity.Message;
import sim.forum.event.comment.CommentCreateEvent;
import sim.forum.event.comment.CommentDeleteEvent;
import sim.forum.event.comment.CommentRestoreEvent;
import sim.forum.event.rating.ToggleRatingEvent;
import sim.forum.exception.BusinessException;
import sim.forum.mapper.CommentMapper;
import sim.forum.result.PageResult;
import sim.forum.service.CommentService;
import sim.forum.service.CountService;
import sim.forum.service.MessageService;
import sim.forum.service.PostService;
import sim.forum.vo.comment.CommentVO;

import java.util.*;

@Transactional
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private CountService countService;
    @Autowired
    private PostService postService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private MessageService messageService;
    @Override
    public Comment createComment(CommentDTO dto, Long userId) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(dto, comment);
        comment.setCreator(userId);
        commentMapper.insert(comment);

        // 直接对事件进行广播
        eventPublisher.publishEvent(new CommentCreateEvent(userId, dto.getRootId() , Comment.CommentTarget.POST));

        // 发送消息
        Long receiverId = null;
        if (dto.getTarget() == Comment.CommentTarget.POST) {
            receiverId = postService.getUserIdByPostId(dto.getTargetId());
        }else if (dto.getTarget() == Comment.CommentTarget.COMMENT) {
            receiverId = this.getUserIdByCommentId(dto.getTargetId());
        }
        if (receiverId == null) {
            log.warn("未能找到消息接收者，可能原内容已被删除。发送人: {}, 载体ID: {}", userId, dto.getTargetId());
            return commentMapper.selectById(comment.getId()); // 正常返回评论，不发通知
        }
        log.info("发送人: {}, 接收人:{}, 内容:{}", userId, receiverId, dto.getContent());
        SendMessageDTO sendDTO = new SendMessageDTO();
        sendDTO.setAction(Message.Action.COMMENT);
        sendDTO.setContent(dto.getContent());
        sendDTO.setSenderId(userId);
        sendDTO.setReceiverId(receiverId);
        sendDTO.setTarget(dto.getTarget().name());
        sendDTO.setTargetId(dto.getTargetId());
        messageService.send(sendDTO);
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

    @Override
    public Long getUserIdByCommentId(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        return comment == null? null : comment.getCreator();
    }

    @EventListener(condition = "#event.target == T(sim.forum.entity.Rating$RatingType).COMMENT")
    public void updatePostLikeCount(ToggleRatingEvent event) {
        countService.updateAtomicCount(commentMapper, event.targetId(),
                "like_count", event.delta(), true);
    }
}
