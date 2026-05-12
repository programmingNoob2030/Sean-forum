package sim.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sim.forum.entity.BoardMember;
import sim.forum.mapper.BoardMemberMapper;
import sim.forum.service.BoardMemberService;

import java.util.List;

@Service
public class BoardMemberServiceImpl implements BoardMemberService{
    @Autowired
    private BoardMemberMapper boardMemberMapper;
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
}
