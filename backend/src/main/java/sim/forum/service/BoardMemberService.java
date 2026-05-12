package sim.forum.service;

import sim.forum.entity.BoardMember;

import java.util.List;

public interface BoardMemberService {
    void addMember(BoardMember member);

    List<BoardMember> getBoardsByMemberId(Long memberId);

    BoardMember getUserBoard(Long boardId, Long MemberId);
}
