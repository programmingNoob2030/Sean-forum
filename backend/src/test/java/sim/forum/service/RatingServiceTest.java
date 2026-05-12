package sim.forum.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sim.forum.context.UserContext;
import sim.forum.dto.rating.RatingDTO;
import sim.forum.entity.Rating;
import sim.forum.exception.BusinessException;

@SpringBootTest
public class RatingServiceTest {
    @Autowired
    private RatingService ratingService;

    @Test
    void testCreateLike(){
        RatingDTO dto = new RatingDTO();
        UserContext.setUserId(11L);
        dto.setTarget(Rating.RatingType.POST);
        dto.setTargetId(20L);

        // 是否执行操作
        Rating rating = ratingService.handleVote(dto);
        Assertions.assertNotNull(rating);

        // 是否创建成功
        Long id = rating.getId();
        Assertions.assertNotNull(id);
    }
//    @Test
//    void testDeleteLike(){
//        DeleteRatingDTO dto = new DeleteRatingDTO();
//        UserContext.setUserId(11L);
//        dto.setId(11L);
//        Rating rating = ratingService.selectById(dto.getId());
//        // 确认喜欢被删除之前是存在的
//        Assertions.assertNotNull(rating);
//        // 删除
//        ratingService.deleteLike(dto);
//        // 2. 断言：调用查询时【必须】抛出 BusinessException
//        // 如果 selectById 没抛异常，说明没删掉，测试反而会失败
//        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> {
//            ratingService.selectById(dto.getId()); // 假设这是你封装的带异常检查的查询
//        });
//    }
//
//    @Test
//    void testRestoreLike(){
//        UserContext.setUserId(11L);
//
//        RestoreRatingDTO dto = new RestoreRatingDTO();
//        dto.setId(11L);
////        Post p = postService.selectById(dto.getId());
//        // 确认p被恢复之前是不存在的
////        Assertions.assertNull(p);
//        // 恢复
//        ratingService.restoreLike(dto);
//        // 是否恢复成功
//        Rating rating = ratingService.selectById(dto.getId());
//        Assertions.assertNotNull(rating);
//    }
}
