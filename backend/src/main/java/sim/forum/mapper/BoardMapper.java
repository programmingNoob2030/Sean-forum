package sim.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sim.forum.entity.Board;
import sim.forum.vo.board.BoardVO;
import sim.forum.vo.board.BriefBoardVO;

import java.util.List;

@Mapper
public interface BoardMapper extends BaseMapper<Board> {
    BoardVO getBoardDetail(Long id, Long userId);

    List<BriefBoardVO> searchBoardsByKeyword(String keyword);

    List<BriefBoardVO> getRecentBoards(List<Long> ids, Long userId);
}
