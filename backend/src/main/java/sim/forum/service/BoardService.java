package sim.forum.service;

import org.springframework.web.multipart.MultipartFile;
import sim.forum.dto.board.BoardDTO;
import sim.forum.entity.Board;
import sim.forum.vo.board.BoardVO;
import sim.forum.vo.board.BriefBoardVO;
import sim.forum.vo.board.SquareBoardVO;

import java.util.List;

public interface BoardService {
    String uploadCover(MultipartFile file);

    Board createBoard(BoardDTO dto, Long creator);

    List<BriefBoardVO> getBoardsByMemberId(Long userId);

    BoardVO getBoardDetail(Long id, Long userId);

    List<BriefBoardVO> searchBoardsByKeyword(String keyword);

    List<SquareBoardVO> getSquareBoards(Long userId);
}
