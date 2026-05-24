package sim.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import sim.forum.context.UserContext;
import sim.forum.dto.message.SendMessageDTO;
import sim.forum.dto.rating.RatingDTO;
import sim.forum.entity.Message;
import sim.forum.entity.Rating;
import sim.forum.event.rating.ToggleRatingEvent;
import sim.forum.exception.BusinessException;
import sim.forum.mapper.RatingMapper;
import sim.forum.service.CommentService;
import sim.forum.service.MessageService;
import sim.forum.service.PostService;
import sim.forum.service.RatingService;
import sim.forum.vo.rating.RatingVO;

@Slf4j
@Service
@Transactional
@Validated
public class RatingServiceImpl implements RatingService {
    @Autowired
    private RatingMapper ratingMapper;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private PostService postService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private CommentService commentService;

    @Override
    public Rating selectById(Long id) {
        Rating rating = ratingMapper.selectById(id);
        if (rating == null) throw new BusinessException("此条喜欢记录不存在");
        return rating;
    }

    public RatingVO handleVote(RatingDTO dto, Long userId) {
        // 获取当前状态 (Current State)
        Integer current = mappingState(userId, dto.getTargetId(), dto.getTarget()); // 当前记录的状态 点赞 空 拉踩 : 1 0 -1
        current = current == -2 ? 0 : current;
        Integer intent = (dto.getAction() == 1) ? 1 : -1; // 用户当前操作 点赞 拉踩 : 1 -1

        // 计算目标状态 (Target State: Toggle logic)
        Integer target = (intent == current) ? 0 : intent; // 最终态 用户操作和已经存在的状态同 0 否则执行正常操作

        // 计算偏移量 (The Magic Delta)
        Integer delta = target - current; // 偏移量 从当前状态 变为 最终态 需要的代价 无需判断
        // 只需要关心 delta>0 增加喜欢数delta delta<0 减少喜欢数delta
        // 触发转换 (Execute Transition)

        LambdaQueryWrapper<Rating> query = new LambdaQueryWrapper<Rating>()
                .eq(Rating::getCreator,UserContext.getUserId())
                .eq(Rating::getTargetId, dto.getTargetId())
                .eq(Rating::getTarget, dto.getTarget());
        Rating r = ratingMapper.selectOne(query);
        Rating rating = new Rating();
        if (delta != 0) {
            if (r == null){
                rating.setType(target);
                rating.setCreator(UserContext.getUserId());
                rating.setTarget(dto.getTarget());
                rating.setTargetId(dto.getTargetId());
                ratingMapper.insert(rating);
            }else{
                r.setType(target);
                ratingMapper.updateById(r);
            }
            eventPublisher.publishEvent(new ToggleRatingEvent(dto.getTargetId(), dto.getTarget(), delta));
        }
        Integer newCount = 0;
        if (dto.getTarget() == Rating.RatingType.COMMENT){
            newCount = commentService.getCommentLikeCountById(dto.getTargetId());
        }else if (dto.getTarget() == Rating.RatingType.POST){
            newCount = postService.getPostLikeCountById(dto.getTargetId());
        }
        RatingVO ratingVO = new RatingVO();
        if (r == null) {
            BeanUtils.copyProperties(rating, ratingVO);
        } else {
            BeanUtils.copyProperties(r, ratingVO);
        }
        ratingVO.setLikeCount(newCount);
        if (target == 0) return ratingVO;

        // 发送消息
        SendMessageDTO sendDTO = new SendMessageDTO();
        if (target < 0) sendDTO.setAction(Message.Action.DISLIKE);
        else sendDTO.setAction(Message.Action.LIKE);
        sendDTO.setContent(null);
        sendDTO.setSenderId(userId);
        Long receiverId = null;
        if (dto.getTarget() == Rating.RatingType.COMMENT){
            receiverId = commentService.getUserIdByCommentId(dto.getTargetId());
        }else if (dto.getTarget() == Rating.RatingType.POST){
            receiverId = postService.getUserIdByPostId(dto.getTargetId());
        }
        if (receiverId == null) {
            log.warn("未能找到消息接收者，可能原内容已被删除。发送人: {}, 载体ID: {}", userId, dto.getTargetId());
        }
        sendDTO.setReceiverId(receiverId);
        sendDTO.setTarget(dto.getTarget().name());
        sendDTO.setTargetId(dto.getTargetId());
        messageService.send(sendDTO);
        return ratingVO;
    }
    public Integer mappingState(Long creator, Long targetId, Rating.RatingType target){
        LambdaQueryWrapper<Rating> query = new LambdaQueryWrapper<Rating>()
                .eq(Rating::getCreator, creator)
                .eq(Rating::getTargetId, targetId)
                .eq(Rating::getTarget, target);
        Rating rating = ratingMapper.selectOne(query);
        if (rating == null) return -2;
        return rating.getType();
        // 0 中和 1 点赞 -1 拉踩 -2 无任何记录

    }
}
