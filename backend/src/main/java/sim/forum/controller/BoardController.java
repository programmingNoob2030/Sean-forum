package sim.forum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sim.forum.annotation.OptionalAuth;
import sim.forum.context.UserContext;
import sim.forum.dto.board.BoardDTO;
import sim.forum.dto.board.BoardMemberDTO;
import sim.forum.entity.Board;
import sim.forum.entity.BrowseRecord;
import sim.forum.result.Result;
import sim.forum.service.BoardMemberService;
import sim.forum.service.BoardService;
import sim.forum.service.BrowseRecordService;
import sim.forum.vo.board.BoardMembershipVO;
import sim.forum.vo.board.BriefBoardVO;
import sim.forum.vo.board.SquareBoardVO;

import java.util.List;

@RestController
public class BoardController {
    @Autowired
    private BoardService boardService;

    @Autowired
    private BrowseRecordService browseRecordService;

    @Autowired
    private BoardMemberService boardMemberService;

    @PostMapping("/board/cover")
    public Result<String> uploadCover(@RequestParam("file") MultipartFile file) {
        // 1. 从 ThreadLocal/BaseContext 获取当前登录用户 ID
        // 假设你的拦截器已经把解析好的 JWT ID 放进去了
        // 2. 一行代码解决战斗
        String avatarPath = boardService.uploadCover(file);
        // 3. 返回成功结果
        return Result.success(avatarPath);
    }
    @PostMapping("/boards")
    public Result<Board> createBoard(@RequestBody BoardDTO dto){
        Long creator = UserContext.getUserId();
        Board board = boardService.createBoard(dto, creator);
        return Result.success(board);
    }
    @GetMapping("/boards/mine")
    public Result<List<BriefBoardVO>> getMyBoards(){
        Long userId = UserContext.getUserId();
        List<BriefBoardVO> list = boardService.getBoardsByMemberId(userId);
        return Result.success(list);
    }

    @GetMapping("/board/{id}")
    @OptionalAuth
    public Result<Board> getBoardById(@PathVariable Long id){
        Long userId = UserContext.getUserId();
        Board board = boardService.getBoardDetail(id, userId);
        return Result.success(board);
    }

    @GetMapping("/board/search")
    public Result<List<BriefBoardVO>> searchBoardByKeyword(String keyword){
        List<BriefBoardVO> list = boardService.searchBoardsByKeyword(keyword);
        return Result.success(list);
    }

    @GetMapping("/boards")
    @OptionalAuth
    public Result<List<SquareBoardVO>> getSquareBoards(){
        Long userId = UserContext.getUserId();
        List<SquareBoardVO> squareBoards = boardService.getSquareBoards(userId);
        return Result.success(squareBoards);
    }

    @PutMapping("/board/membership")
    public Result<BoardMembershipVO> toggleBoardMembership(@RequestBody BoardMemberDTO dto){
        Long userId = UserContext.getUserId();
        BoardMembershipVO vo = boardMemberService.handleMembership(dto, userId);
        return Result.success(vo);
    }

    @GetMapping("/boards/history")
    public Result<List<BriefBoardVO>> getBoardsHistory(){
        Long userId = UserContext.getUserId();
        // 解除 BoardService 和 BrowseRecordService的循环引用
        List<Long> ids = browseRecordService.getIdsFromRedis(BrowseRecord.BrowseTarget.BOARD, userId);
        List<BriefBoardVO> list = boardService.getRecentBoards(ids, userId);
        return Result.success(list);
    }

}
