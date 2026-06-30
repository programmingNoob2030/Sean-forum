package sim.forum.service;

import sim.forum.dto.board.BoardMemberDTO;
import sim.forum.entity.BoardMember;
import sim.forum.vo.board.BoardMembershipVO;

import java.util.List;

public interface BoardMemberService {
    void addMember(BoardMember member);

    List<BoardMember> getBoardsByMemberId(Long memberId);

    BoardMember getUserBoard(Long boardId, Long MemberId);

    BoardMembershipVO handleMembership(BoardMemberDTO dto, Long userId);
}
