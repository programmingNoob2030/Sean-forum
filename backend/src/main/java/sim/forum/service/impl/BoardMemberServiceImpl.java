package sim.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sim.forum.dto.board.BoardMemberDTO;
import sim.forum.entity.Board;
import sim.forum.entity.BoardMember;
import sim.forum.event.board.ToggleBoardMemberEvent;
import sim.forum.exception.BusinessException;
import sim.forum.mapper.BoardMapper;
import sim.forum.mapper.BoardMemberMapper;
import sim.forum.service.BoardMemberService;
import sim.forum.vo.board.BoardMembershipVO;

import java.util.List;

@Service
public class BoardMemberServiceImpl implements BoardMemberService{
    @Autowired
    private BoardMemberMapper boardMemberMapper;
    @Autowired
    private BoardMapper boardMapper;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public void addMember(BoardMember member) {
        boardMemberMapper.insert(member);
    }

    @Override
    public List<BoardMember> getBoardsByMemberId(Long memberId) {
        LambdaQueryWrapper<BoardMember> query = new LambdaQueryWrapper<BoardMember>()
                .eq(BoardMember::getMemberId, memberId);
        return boardMemberMapper.selectList(query);
    }

    @Override
    public BoardMember getUserBoard(Long boardId, Long memberId) {
        LambdaQueryWrapper<BoardMember> query = new LambdaQueryWrapper<BoardMember>()
                .eq(BoardMember::getMemberId, memberId)
                .eq(BoardMember::getBoardId, boardId);
        return boardMemberMapper.selectOne(query);
    }

    @Override
    @Transactional
    public BoardMembershipVO handleMembership(BoardMemberDTO dto, Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        if (dto == null || dto.getBoardId() == null) {
            throw new BusinessException("社区ID不能为空");
        }

        Board board = boardMapper.selectById(dto.getBoardId());
        if (board == null) {
            throw new BusinessException("目标社区不存在");
        }

        BoardMember record = getUserBoard(dto.getBoardId(), userId);
        if (record != null && (record.getRole() == BoardMember.RoleType.CREATOR
                || record.getRole() == BoardMember.RoleType.ADMIN)) {
            return buildMembershipVO(board.getId(), record.getRole(),
                    board.getMemberCount() == null ? 0 : board.getMemberCount());
        }

        int current = mappingState(record);
        int intent = dto.getAction() != null && dto.getAction() == 1 ? 1 : 0;
        int target = intent == current ? 0 : intent;
        int delta = target - current;

        if (delta != 0) {
            if (target == 0) {
                boardMemberMapper.deleteById(record.getId());
            } else {
                BoardMember member = new BoardMember();
                member.setBoardId(dto.getBoardId());
                member.setMemberId(userId);
                member.setRole(BoardMember.RoleType.MEMBER);
                boardMemberMapper.insert(member);
            }
            eventPublisher.publishEvent(new ToggleBoardMemberEvent(dto.getBoardId(), delta));
        }

        Integer memberCount = board.getMemberCount() == null ? 0 : board.getMemberCount();
        return buildMembershipVO(board.getId(), target == 1 ? BoardMember.RoleType.MEMBER : null,
                Math.max(0, memberCount + delta));
    }

    private int mappingState(BoardMember record) {
        return record == null ? 0 : 1;
    }

    private BoardMembershipVO buildMembershipVO(Long boardId, BoardMember.RoleType role, Integer memberCount) {
        BoardMembershipVO vo = new BoardMembershipVO();
        vo.setBoardId(boardId);
        vo.setRole(role);
        vo.setMemberCount(memberCount);
        return vo;
    }
}
