package sim.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sim.forum.dto.board.BoardDTO;
import sim.forum.dto.browserecord.CreateBrowseRecordDTO;
import sim.forum.entity.Board;
import sim.forum.entity.BoardMember;
import sim.forum.entity.BrowseRecord;
import sim.forum.entity.User;
import sim.forum.event.post.PostCreateEvent;
import sim.forum.exception.BusinessException;
import sim.forum.mapper.BoardMapper;
import sim.forum.service.*;
import sim.forum.vo.board.BoardVO;
import sim.forum.vo.board.BriefBoardVO;
import sim.forum.vo.board.SquareBoardVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BoardServiceImpl implements BoardService {
    @Autowired
    private FileUploadService fileUploadService;
    @Autowired
    private BoardMapper boardMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private BoardMemberService boardMemberService;
    @Autowired
    private CountService countService;
    @Autowired
    private BrowseRecordService browseRecordService;
    @Override
    public String uploadCover(MultipartFile file) {
        String relativePath = fileUploadService.upload(file, "board/cover");
        log.info("社区封面更新成功: {}", relativePath);
        return relativePath;
    }
    private Board isBoardNameUsed(String name){
        LambdaQueryWrapper<Board> query = new LambdaQueryWrapper<Board>()
                .eq(Board::getName, name);
        return boardMapper.selectOne(query);
    }
    @Override
    public Board createBoard(BoardDTO dto, Long creator) {
        Board board = isBoardNameUsed(dto.getName());
        if (board != null) throw new BusinessException("社区名字已存在!");
        board = new Board();
        // 创建社区
        board.setName(dto.getName());
        board.setCover(dto.getCover());
        board.setDescription(dto.getDescription());
        board.setType(dto.getType());
        board.setCreator(creator);
        boardMapper.insert(board);
        board = isBoardNameUsed(dto.getName());
        // 更新社区的成员
        BoardMember member = new BoardMember();
        member.setBoardId(board.getId());
        member.setMemberId(creator);
        member.setRole(BoardMember.RoleType.CREATOR);
        boardMemberService.addMember(member);
        return board;
    }
    @Override
    public List<BriefBoardVO> getBoardsByMemberId(Long userId) {
        User user = userService.selectUserById(userId);
        if (user == null) throw new BusinessException("此用户不存在!");
        List<BoardMember> boards = boardMemberService.getBoardsByMemberId(userId);
        List<BriefBoardVO> briefBoards = new ArrayList<>();
        if (boards == null) return briefBoards;
        for (BoardMember board : boards){
            Board b = boardMapper.selectById(board.getBoardId());
            BriefBoardVO vo = new BriefBoardVO();
            BeanUtils.copyProperties(b, vo);
            vo.setRole(board.getRole());
            briefBoards.add(vo);
        }
        return briefBoards;
    }

    @Override
    public BoardVO getBoardDetail(Long id, Long userId) {
        BoardVO boardVO = boardMapper.getBoardDetail(id, userId);
        if (boardVO == null) {
            throw new BusinessException("目标社区不存在!");
        }
        if (boardVO.getCurrentUserRole() == null) boardVO.setCurrentUserRole("GUEST");
        CreateBrowseRecordDTO dto = new CreateBrowseRecordDTO();
        dto.setTarget(BrowseRecord.BrowseTarget.BOARD);
        dto.setTargetId(id);
        if(userId != null){
            browseRecordService.saveRecordAsync(dto, userId);
        }
        return boardVO;
    }

    @Override
    public List<BriefBoardVO> searchBoardsByKeyword(String keyword) {
        List<BriefBoardVO> list = boardMapper.searchBoardsByKeyword(keyword);
        if (list == null) return new ArrayList<>();
        return list;
    }

    @Override
    public List<BriefBoardVO> getRecentBoards(List<Long> ids, Long userId) {
        if (userId == null) throw new BusinessException("此用户信息有误,无法查询!");
        return boardMapper.getRecentBoards(ids, userId);
    }

    @Override
    public List<SquareBoardVO> getSquareBoards(Long userId) {
        List<BoardMember> boardMembers = boardMemberService.getBoardsByMemberId(userId);
        List<Board> boards = boardMapper.selectList(null);
        // 空间换时间
        Map<Long,BoardMember> boardMembersMap = boardMembers
                .stream()
                .collect(Collectors.toMap(BoardMember::getId, boardMember -> boardMember));

        List<SquareBoardVO> squareBoards = new ArrayList<>();
        // 遍历
        for (Board board : boards){
            SquareBoardVO vo = new SquareBoardVO();
            BeanUtils.copyProperties(board, vo);
            BoardMember record = boardMembersMap.get(board.getId());
            BoardMember.RoleType role = null;
            if (record != null){
                role = record.getRole();
            }
            if (role != null){
                vo.setRole(role);
            }
            squareBoards.add(vo);
        }
        return squareBoards;
    }
    @EventListener
    public void addPostCount(PostCreateEvent event) {
        countService.updateAtomicCount(boardMapper, event.boardId(),
                "post_count", 1,true);
    }
}
